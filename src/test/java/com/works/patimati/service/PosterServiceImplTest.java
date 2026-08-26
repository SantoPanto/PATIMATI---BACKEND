package com.works.patimati.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.impl.PosterServiceImpl;
import com.works.patimati.storage.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PosterServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private PosterServiceImpl posterService;

    private Ad testAd;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .uid(1L)
                .firstName("Ali")
                .lastName("Yilmaz")
                .email("ali@example.com")
                .phone("05551112233")
                .build();

        testAd = Ad.builder()
                .id(100L)
                .title("Kayip Tekir Kedi")
                .description("Kadikoy taraflarinda kayboldu")
                .adType(Ad.AdType.LOST)
                .species(Species.CAT)
                .breed("Tekir")
                .gender(PetGender.MALE)
                .ageGroup(AgeGroup.ADULT)
                .user(user)
                .active(true)
                .isPosterAllowed(true)
                .showEmailOnPoster(true)
                .showPhoneOnPoster(true)
                .build();
    }

    @Test
    void generateAdPosterPdf_ShouldReturnNonEmptyByteArray_WhenAdExistsAndPosterAllowed() {
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(adRepository, times(1)).findById(100L);
    }

    @Test
    void generateAdPosterPdf_ShouldThrowAccessDeniedException_WhenNotOwnerAndPosterNotAllowed() {
        testAd.setIsPosterAllowed(false);
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> posterService.generateAdPosterPdf(100L, "baskasi@example.com"));
    }

    @Test
    void generateAdPosterPdf_ShouldAllowOwner_EvenIfPosterNotAllowed() {
        testAd.setIsPosterAllowed(false);
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateAdPosterPdf_ShouldSupportTurkishCharacters_WhenAdContainsTurkishText() {
        User turkishUser = User.builder()
                .uid(2L)
                .firstName("Çağrı")
                .lastName("Öztürk")
                .email("cagri@example.com")
                .phone("05559998877")
                .build();

        Ad turkishAd = Ad.builder()
                .id(101L)
                .title("Kayıp Şirin Köpek - Çoşkun")
                .description("Kayıp olduğu tarih: 2026. Görenlerin ulaşması rica olunur. Çağrı / Öztürk ailesi.")
                .distinctiveMarks("Sol kulağında çentik, sırtında kahverengi benek var.")
                .adType(Ad.AdType.LOST)
                .species(Species.DOG)
                .breed("Kangal Mix")
                .gender(PetGender.FEMALE)
                .ageGroup(AgeGroup.ADULT)
                .user(turkishUser)
                .active(true)
                .isPosterAllowed(true)
                .showEmailOnPoster(true)
                .showPhoneOnPoster(true)
                .build();

        when(adRepository.findById(101L)).thenReturn(Optional.of(turkishAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(101L, "cagri@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(adRepository, times(1)).findById(101L);
    }

    @Test
    void generateAdPosterPdf_EnumAlanlariniTurkceBassin_HamEnumSizmasin() throws IOException {
        // 22.08 bulgusu: afişte "Tür: CAT" gibi ham enum adları basılıyordu.
        // PDF metnine demirli: Türkçe etiketler var, ham adlar yok.
        testAd.setColors(new LinkedHashSet<>(List.of(PetColor.BLACK, PetColor.WHITE)));
        testAd.setBreed("MIXED_OR_UNKNOWN");
        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");
        String metin = pdfMetni(pdfBytes);

        assertTrue(metin.contains("KAYIP"), "adType LOST afişte Türkçe 'KAYIP' olmalı");
        assertTrue(metin.contains("Kedi"), "species CAT 'Kedi' olmalı");
        assertTrue(metin.contains("Erkek"), "gender MALE 'Erkek' olmalı");
        assertTrue(metin.contains("Yetişkin"), "ageGroup ADULT 'Yetişkin' olmalı");
        assertTrue(metin.contains("Siyah, Beyaz"), "renkler Türkçe ve girildiği sırada olmalı");
        assertTrue(metin.contains("Cins belirtilmemiş"), "ham MIXED_OR_UNKNOWN çevrilmeli");

        assertFalse(metin.contains("LOST"), "ham adType sızmamalı");
        assertFalse(metin.contains("CAT"), "ham species sızmamalı");
        assertFalse(metin.contains("MALE"), "ham gender sızmamalı");
        assertFalse(metin.contains("ADULT"), "ham ageGroup sızmamalı");
        assertFalse(metin.contains("BLACK"), "ham renk sızmamalı");
        assertFalse(metin.contains("MIXED_OR_UNKNOWN"), "ham ırk sızmamalı");
    }

    @Test
    void generateAdPosterPdf_ShouldEmbedPetPhotoAndKeepQrCodeReadable() throws Exception {
        String photoReference = "s3://patimati-test/ads/2026/08/tekir.jpg";
        testAd.setPhotoUrls(List.of(photoReference));
        testAd.setCity("İstanbul");
        testAd.setDistrict("Kadıköy");
        testAd.setLostDate(LocalDate.of(2026, 8, 24));
        testAd.setColors(new LinkedHashSet<>(List.of(PetColor.BROWN, PetColor.WHITE)));

        when(adRepository.findById(100L)).thenReturn(Optional.of(testAd));
        when(imageStorageService.readImage(photoReference)).thenReturn(testJpeg());

        byte[] pdfBytes = posterService.generateAdPosterPdf(100L, "ali@example.com");

        assertEquals(1, pageCount(pdfBytes), "Afiş tek A4 sayfasında kalmalı");
        assertTrue(embeddedImageCount(pdfBytes) >= 3,
                "PDF'de PatiMati logosu, hayvan fotoğrafı ve QR bulunmalı");
        assertTrue(decodedQrValues(pdfBytes).contains("http://localhost:5173/ads/100"));
        verify(imageStorageService).readImage(photoReference);
    }

    private static String pdfMetni(byte[] pdfBytes) throws IOException {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                sb.append(PdfTextExtractor.getTextFromPage(pdf.getPage(i)));
            }
            return sb.toString();
        }
    }

    private static int pageCount(byte[] pdfBytes) throws IOException {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            return pdf.getNumberOfPages();
        }
    }

    private static int embeddedImageCount(byte[] pdfBytes) throws IOException {
        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            PdfDictionary xObjects = pdf.getPage(1)
                    .getResources()
                    .getPdfObject()
                    .getAsDictionary(PdfName.XObject);
            return xObjects == null ? 0 : xObjects.size();
        }
    }

    private static List<String> decodedQrValues(byte[] pdfBytes) throws IOException {
        List<String> values = new ArrayList<>();

        try (PdfDocument pdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            PdfDictionary xObjects = pdf.getPage(1)
                    .getResources()
                    .getPdfObject()
                    .getAsDictionary(PdfName.XObject);

            if (xObjects == null) {
                return values;
            }

            for (PdfName name : xObjects.keySet()) {
                PdfStream stream = xObjects.getAsStream(name);
                if (stream == null || !PdfName.Image.equals(stream.getAsName(PdfName.Subtype))) {
                    continue;
                }

                BufferedImage image = ImageIO.read(new ByteArrayInputStream(
                        new PdfImageXObject(stream).getImageBytes(true)
                ));
                if (image == null) {
                    continue;
                }

                int width = image.getWidth();
                int height = image.getHeight();
                int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);

                try {
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
                            new RGBLuminanceSource(width, height, pixels)
                    ));
                    values.add(new MultiFormatReader().decode(bitmap).getText());
                } catch (NotFoundException ignored) {
                    // Hayvan fotoğrafı QR olmadığı için bu sonuç beklenir.
                }
            }
        }

        return values;
    }

    private static byte[] testJpeg() throws IOException {
        BufferedImage image = new BufferedImage(900, 650, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(230, 196, 150));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(new Color(90, 61, 38));
            graphics.fillOval(250, 120, 400, 400);
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", output);
            return output.toByteArray();
        }
    }

    @Test
    void generateAdPosterPdf_ShouldSupportTurkishCharacters_WhenAdoptionAdContainsTurkishText() {
        User turkishUser = User.builder()
                .uid(3L)
                .firstName("İbrahim")
                .lastName("Şahin")
                .email("ibrahim@example.com")
                .phone("05553334455")
                .build();

        Ad adoptionAd = Ad.builder()
                .id(102L)
                .title("Sahiplendirilecek Şirin Kedi - Yumuşak")
                .description("İç parazit aşısı yapıldı. Çok sevecen ve oyun sever kedi. Ücret talep edilmeyecektir.")
                .distinctiveMarks("Beyaz tüy, sarı gözlü. Sahiplendirme ilanımızdır.")
                .adType(Ad.AdType.ADOPTION)
                .species(Species.CAT)
                .breed("Van Kedisi")
                .gender(PetGender.MALE)
                .ageGroup(AgeGroup.YOUNG)
                .user(turkishUser)
                .active(true)
                .isPosterAllowed(true)
                .showEmailOnPoster(true)
                .showPhoneOnPoster(true)
                .build();

        when(adRepository.findById(102L)).thenReturn(Optional.of(adoptionAd));

        byte[] pdfBytes = posterService.generateAdPosterPdf(102L, "ibrahim@example.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        verify(adRepository, times(1)).findById(102L);
    }

    @Test
    void generateAdPosterPdf_ShouldThrowResourceNotFoundException_WhenAdDoesNotExist() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> posterService.generateAdPosterPdf(999L));
        verify(adRepository, times(1)).findById(999L);
    }
}
