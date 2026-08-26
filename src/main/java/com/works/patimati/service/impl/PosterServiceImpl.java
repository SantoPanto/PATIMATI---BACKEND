package com.works.patimati.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.PosterService;
import com.works.patimati.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * İlan verisini PatiMati tasarım dilini taşıyan, tek sayfalık ve QR kodlu
 * yazdırılabilir PDF afişe dönüştürür.
 *
 * <p>Fotoğraf doğrudan uygulamanın güvenilir depolama katmanından okunur.
 * Böylece PDF üreticisi istemciden gelen rastgele bir URL'e istek atmaz.</p>
 */
@Service
@RequiredArgsConstructor
public class PosterServiceImpl implements PosterService {

    private static final Logger log = LoggerFactory.getLogger(PosterServiceImpl.class);

    // PatiMati web tasarım sistemindeki ana renkler.
    private static final DeviceRgb BRAND_ORANGE = new DeviceRgb(249, 115, 22);
    private static final DeviceRgb BRAND_ORANGE_DARK = new DeviceRgb(234, 88, 12);
    private static final DeviceRgb BRAND_ORANGE_SOFT = new DeviceRgb(255, 247, 237);
    private static final DeviceRgb PAGE_BACKGROUND = new DeviceRgb(255, 251, 247);
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(15, 23, 42);
    private static final DeviceRgb MUTED_TEXT_COLOR = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(226, 232, 240);
    private static final DeviceRgb LOCATION_BLUE = new DeviceRgb(37, 99, 235);
    private static final DeviceRgb LOCATION_BLUE_SOFT = new DeviceRgb(239, 246, 255);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String BRAND_LOGO_RESOURCE = "/images/patimati-logo.png";
    private static final float QR_DISPLAY_SIZE = 118f;
    private static final int QR_IMAGE_SIZE = 420;

    private final AdRepository adRepository;
    private final ImageStorageService imageStorageService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    @Override
    public byte[] generateAdPosterPdf(Long adId) {
        String requestingUserEmail = null;
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            requestingUserEmail = auth.getName();
        }
        return generateAdPosterPdf(adId, requestingUserEmail);
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] generateAdPosterPdf(Long adId, String requestingUserEmail) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));

        boolean isOwner = requestingUserEmail != null
                && ad.getUser() != null
                && requestingUserEmail.equalsIgnoreCase(ad.getUser().getEmail());

        if (!isOwner && !Boolean.TRUE.equals(ad.getIsPosterAllowed())) {
            throw new AccessDeniedException("Afiş oluşturma kapalıdır");
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(output));
            pdfDocument.addNewPage(PageSize.A4);
            addPageBackground(pdfDocument);

            Document document = new Document(pdfDocument, PageSize.A4);
            // A4 afişte fotoğraflı ilanların da ikinci sayfaya taşmaması için
            // güvenli baskı payını koruyan kompakt üst/alt kenar boşlukları.
            document.setMargins(18, 28, 14, 28);

            PdfFont regularFont = loadTurkishFont(false);
            PdfFont boldFont = loadTurkishFont(true);
            PdfFont brandFont = loadBrandFont(boldFont);
            document.setFont(regularFont);

            PosterTheme theme = themeFor(ad.getAdType());

            addBrandHeader(document, ad, theme, regularFont, boldFont, brandFont);
            addIntroSection(document, ad, theme, regularFont, boldFont);
            addPhotoSection(document, ad, regularFont, boldFont);
            addQuickFacts(document, ad, theme, regularFont, boldFont);
            addDetailsSection(document, ad, regularFont, boldFont);
            addContactAndQrSection(document, ad, theme, regularFont, boldFont);
            addFooter(document, regularFont, boldFont);

            document.close();
            log.info("İlan afişi PDF başarıyla üretildi. adId={}", adId);
            return output.toByteArray();
        } catch (AccessDeniedException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("İlan afişi PDF üretilirken hata oluştu. adId={}", adId, exception);
            throw new RuntimeException("Afiş PDF üretilemedi: " + exception.getMessage(), exception);
        }
    }

    /** Afişin web sitesindeki sıcak krem zeminini tüm A4 sayfasına uygular. */
    private void addPageBackground(PdfDocument pdfDocument) {
        PdfCanvas canvas = new PdfCanvas(pdfDocument.getFirstPage());
        canvas.saveState();
        canvas.setFillColor(PAGE_BACKGROUND);
        canvas.rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight());
        canvas.fill();
        canvas.restoreState();
    }

    /**
     * Web sitesindeki turuncu marka kimliğini ve ilan durum rozetini PDF'in
     * en üstünde tekrarlar.
     */
    private void addBrandHeader(
            Document document,
            Ad ad,
            PosterTheme theme,
            PdfFont regularFont,
            PdfFont boldFont,
            PdfFont brandFont
    ) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{67, 33}))
                .useAllAvailableWidth()
                .setMarginBottom(6);

        Table brandIdentity = new Table(UnitValue.createPercentArray(new float[]{14, 86}))
                .useAllAvailableWidth();

        // Marka alanında temsili bir harf yerine PatiMati'nin gerçek logosu kullanılır.
        // Logo classpath'ten okunduğu için sunucu dosya sistemine bağımlı değildir.
        Image brandLogo = loadBrandLogo();

        Cell brandIcon = new Cell()
                .add(brandLogo)
                .setWidth(38)
                .setHeight(38)
                .setPadding(0)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Paragraph brandName = new Paragraph()
                // İki renkli kelime işareti aynı Paragraph akışında tutulur.
                // Böylece "PATI" sonundaki I harfi turuncu M ile çakışmaz.
                .add(new Text("PATI").setFont(brandFont).setFontColor(TEXT_COLOR))
                .add(new Text("MATI").setFont(brandFont).setFontColor(BRAND_ORANGE))
                .setFontSize(18)
                .setMarginTop(1)
                .setMarginBottom(0);

        Cell brandText = new Cell()
                .add(brandName)
                .add(new Paragraph("Minik dostlarımız için birlikte")
                        .setFont(regularFont)
                        .setFontSize(7.8f)
                        .setFontColor(MUTED_TEXT_COLOR)
                        .setMarginTop(0)
                        .setMarginBottom(0))
                .setPaddingLeft(9)
                .setPaddingTop(0)
                .setPaddingBottom(0)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        brandIdentity.addCell(brandIcon);
        brandIdentity.addCell(brandText);

        Cell left = new Cell()
                .add(brandIdentity)
                .setPadding(0)
                .setBorder(Border.NO_BORDER);

        Cell status = new Cell()
                .add(new Paragraph(theme.statusLabel() + "   #" + ad.getId())
                        .setFont(boldFont)
                        .setFontSize(9)
                        .setFontColor(theme.statusAccent())
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMargin(0))
                .setPaddingTop(9)
                .setPaddingBottom(9)
                .setPaddingLeft(8)
                .setPaddingRight(8)
                .setBackgroundColor(theme.statusSoft())
                .setBorder(new SolidBorder(theme.statusBorder(), 0.9f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        Cell right = new Cell()
                .add(new Table(1).useAllAvailableWidth().addCell(status))
                .setPaddingLeft(18)
                .setPaddingTop(3)
                .setBorder(Border.NO_BORDER);

        header.addCell(left);
        header.addCell(right);
        document.add(header);
    }

    /**
     * Proje kaynaklarına eklenen gerçek PatiMati logosunu PDF'e gömülecek
     * iText görseline dönüştürür.
     */
    private Image loadBrandLogo() {
        try (InputStream input = getClass().getResourceAsStream(BRAND_LOGO_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("PatiMati logo kaynağı bulunamadı: " + BRAND_LOGO_RESOURCE);
            }

            return new Image(ImageDataFactory.create(input.readAllBytes()))
                    .setWidth(38)
                    .setHeight(38)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
        } catch (Exception exception) {
            throw new IllegalStateException("PatiMati logosu PDF'e yüklenemedi", exception);
        }
    }

    /** Büyük başlık ve kısa dayanışma mesajıyla afişin görsel hiyerarşisini kurar. */
    private void addIntroSection(
            Document document,
            Ad ad,
            PosterTheme theme,
            PdfFont regularFont,
            PdfFont boldFont
    ) {
        document.add(new Paragraph(theme.eyebrow())
                .setFont(boldFont)
                .setFontSize(8.5f)
                .setFontColor(BRAND_ORANGE)
                .setMarginTop(0)
                .setMarginBottom(3));

        document.add(new Paragraph(truncate(ad.getTitle(), 72, theme.fallbackTitle()))
                .setFont(boldFont)
                .setFontSize(25)
                .setFontColor(TEXT_COLOR)
                .setFixedLeading(28)
                .setMarginTop(0)
                .setMarginBottom(3));

        document.add(new Paragraph(theme.introSubtitle())
                .setFont(regularFont)
                .setFontSize(10.5f)
                .setFontColor(MUTED_TEXT_COLOR)
                .setMarginTop(0)
                .setMarginBottom(6));
    }

    /**
     * İlanın ilk okunabilir fotoğrafını afişin ana görseli yapar. Fotoğrafın
     * tamamı gösterilir; hayvana ait ayırt edici bölümler kırpılmaz.
     */
    private void addPhotoSection(Document document, Ad ad, PdfFont regularFont, PdfFont boldFont) {
        Cell photoCell = new Cell()
                .setHeight(226)
                .setPadding(8)
                .setBackgroundColor(WHITE)
                .setBorder(new SolidBorder(BORDER_COLOR, 1f))
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);

        Image petPhoto = loadFirstAvailablePhoto(ad);
        if (petPhoto != null) {
            petPhoto.scaleToFit(515, 208);
            petPhoto.setHorizontalAlignment(HorizontalAlignment.CENTER);
            photoCell.add(petPhoto);
        } else {
            photoCell.add(new Paragraph("İLAN FOTOĞRAFI")
                    .setFont(boldFont)
                    .setFontSize(13)
                    .setFontColor(BRAND_ORANGE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));
            photoCell.add(new Paragraph("Bu ilana henüz fotoğraf eklenmemiş.")
                    .setFont(regularFont)
                    .setFontSize(9)
                    .setFontColor(MUTED_TEXT_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMargin(0));
        }

        Table photo = new Table(1)
                .useAllAvailableWidth()
                .setKeepTogether(true)
                .setMarginBottom(6);
        photo.addCell(photoCell);
        document.add(photo);
    }

    /** Konum, tarih ve temel özellikleri uzaktan da hızlı taranan dört karta ayırır. */
    private void addQuickFacts(
            Document document,
            Ad ad,
            PosterTheme theme,
            PdfFont regularFont,
            PdfFont boldFont
    ) {
        Table facts = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .useAllAvailableWidth()
                .setKeepTogether(true)
                .setMarginBottom(6);

        facts.addCell(quickFactCell(
                "SON GÖRÜLDÜĞÜ YER",
                truncate(locationText(ad), 42, "Konum ilan detayında"),
                LOCATION_BLUE,
                LOCATION_BLUE_SOFT,
                regularFont,
                boldFont
        ));
        facts.addCell(quickFactCell(
                "KAYBOLMA TARİHİ",
                lostDateText(ad),
                BRAND_ORANGE_DARK,
                BRAND_ORANGE_SOFT,
                regularFont,
                boldFont
        ));
        facts.addCell(quickFactCell(
                "TÜR / IRK",
                truncate(speciesAndBreedText(ad), 34, "Belirtilmemiş"),
                BRAND_ORANGE_DARK,
                BRAND_ORANGE_SOFT,
                regularFont,
                boldFont
        ));
        facts.addCell(quickFactCell(
                "CİNSİYET / YAŞ",
                truncate(genderAndAgeText(ad), 34, "Belirtilmemiş"),
                theme.statusAccent(),
                theme.statusSoft(),
                regularFont,
                boldFont
        ));

        document.add(facts);
    }

    private Cell quickFactCell(
            String label,
            String value,
            DeviceRgb accent,
            DeviceRgb background,
            PdfFont regularFont,
            PdfFont boldFont
    ) {
        Cell cell = new Cell()
                .setMinHeight(64)
                .setPaddingTop(8)
                .setPaddingBottom(7)
                .setPaddingLeft(10)
                .setPaddingRight(10)
                .setBackgroundColor(background)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.8f));

        cell.add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(6.7f)
                .setFontColor(accent)
                .setMarginTop(0)
                .setMarginBottom(6));

        cell.add(new Paragraph(value)
                .setFont(boldFont)
                .setFontSize(9.4f)
                .setFontColor(TEXT_COLOR)
                .setFixedLeading(11)
                .setMargin(0));
        return cell;
    }

    /**
     * Serbest metinleri kalabalık bir tablo yerine iki okunabilir kartta
     * toplar ve afişin tek sayfada kalması için kontrollü biçimde kısaltır.
     */
    private void addDetailsSection(
            Document document,
            Ad ad,
            PdfFont regularFont,
            PdfFont boldFont
    ) {
        Table details = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth()
                .setKeepTogether(true)
                .setMarginBottom(6);

        details.addCell(detailCard(
                "AYIRT EDİCİ İŞARETLER",
                truncate(ad.getDistinctiveMarks(), 180, "Belirtilmemiş"),
                regularFont,
                boldFont
        ));

        String colorsAndNotes = colorsText(ad) + ". "
                + truncate(ad.getDescription(), 180, "Ek açıklama bulunmuyor");
        details.addCell(detailCard(
                "RENK VE DİĞER NOTLAR",
                truncate(colorsAndNotes, 220, "Belirtilmemiş"),
                regularFont,
                boldFont
        ));

        document.add(details);
    }

    private Cell detailCard(String label, String value, PdfFont regularFont, PdfFont boldFont) {
        Cell cell = new Cell()
                .setMinHeight(80)
                .setPadding(10)
                .setBackgroundColor(WHITE)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.8f));

        cell.add(new Paragraph(label)
                .setFont(boldFont)
                .setFontSize(7.8f)
                .setFontColor(BRAND_ORANGE_DARK)
                .setMarginTop(0)
                .setMarginBottom(5));

        cell.add(new Paragraph(value)
                .setFont(regularFont)
                .setFontSize(9.2f)
                .setFontColor(TEXT_COLOR)
                .setFixedLeading(12)
                .setMargin(0));
        return cell;
    }

    /**
     * En güçlü eylem çağrısını ve ilan detayına yönlendiren QR kodu tek bir
     * alanda toplar. QR beyaz zeminde, kare oranı ve sessiz alanı korunarak basılır.
     */
    private void addContactAndQrSection(
            Document document,
            Ad ad,
            PosterTheme theme,
            PdfFont regularFont,
            PdfFont boldFont
    ) throws Exception {
        Table contactAndQr = new Table(UnitValue.createPercentArray(new float[]{64, 36}))
                .useAllAvailableWidth()
                .setKeepTogether(true)
                .setMarginBottom(2);

        Cell contactCell = new Cell()
                .setMinHeight(154)
                .setPaddingTop(15)
                .setPaddingBottom(12)
                .setPaddingLeft(16)
                .setPaddingRight(16)
                .setBackgroundColor(BRAND_ORANGE_DARK)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        contactCell.add(new Paragraph(theme.ctaEyebrow())
                .setFont(boldFont)
                .setFontSize(8.5f)
                .setFontColor(BRAND_ORANGE_SOFT)
                .setMarginTop(0)
                .setMarginBottom(4));

        contactCell.add(new Paragraph(theme.ctaTitle())
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(WHITE)
                .setMarginTop(0)
                .setMarginBottom(4));

        contactCell.add(new Paragraph(theme.ctaSubtitle())
                .setFont(regularFont)
                .setFontSize(9.4f)
                .setFontColor(WHITE)
                .setMarginTop(0)
                .setMarginBottom(8));

        addContactLines(contactCell, ad, regularFont, boldFont);

        byte[] qrBytes = generateQrCodeImage(
                posterDetailUrl(ad.getId()),
                QR_IMAGE_SIZE,
                QR_IMAGE_SIZE
        );
        Image qrImage = new Image(ImageDataFactory.create(qrBytes))
                .scaleAbsolute(QR_DISPLAY_SIZE, QR_DISPLAY_SIZE)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Cell qrCell = new Cell()
                .setMinHeight(154)
                .setPaddingTop(7)
                .setPaddingBottom(7)
                .setPaddingLeft(9)
                .setPaddingRight(9)
                .setBackgroundColor(WHITE)
                .setBorder(new SolidBorder(BRAND_ORANGE, 7f))
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        qrCell.add(qrImage);
        qrCell.add(new Paragraph("İLANI AÇ VE BİLDİR")
                .setFont(boldFont)
                .setFontSize(7)
                .setFontColor(TEXT_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(2)
                .setMarginBottom(0));

        contactAndQr.addCell(contactCell);
        contactAndQr.addCell(qrCell);
        document.add(contactAndQr);
    }

    private void addContactLines(Cell contactCell, Ad ad, PdfFont regularFont, PdfFont boldFont) {
        User owner = ad.getUser();
        boolean directContactAdded = false;

        if (owner != null) {
            String contactName = contactName(owner);
            if (!contactName.isBlank()) {
                addContactLine(contactCell, "İletişim", contactName, regularFont, boldFont);
            }

            if (Boolean.TRUE.equals(ad.getShowPhoneOnPoster()) && hasText(owner.getPhone())) {
                addContactLine(contactCell, "Telefon", owner.getPhone(), regularFont, boldFont);
                directContactAdded = true;
            }

            if (Boolean.TRUE.equals(ad.getShowEmailOnPoster()) && hasText(owner.getEmail())) {
                addContactLine(contactCell, "E-posta", owner.getEmail(), regularFont, boldFont);
                directContactAdded = true;
            }
        }

        if (!directContactAdded) {
            contactCell.add(new Paragraph("İletişim için QR kodu okutun.")
                    .setFont(boldFont)
                    .setFontSize(9.6f)
                    .setFontColor(WHITE)
                    .setMarginTop(2)
                    .setMarginBottom(0));
        }
    }

    private void addContactLine(
            Cell cell,
            String label,
            String value,
            PdfFont regularFont,
            PdfFont boldFont
    ) {
        cell.add(new Paragraph(label + ": " + truncate(value, 68, "-"))
                .setFont("İletişim".equals(label) ? boldFont : regularFont)
                .setFontSize(9.6f)
                .setFontColor(WHITE)
                .setMarginTop(0)
                .setMarginBottom(2));
    }

    private void addFooter(Document document, PdfFont regularFont, PdfFont boldFont) {
        Table footer = new Table(UnitValue.createPercentArray(new float[]{75, 25}))
                .useAllAvailableWidth();

        footer.addCell(new Cell()
                .add(new Paragraph("Bu afişi paylaş, bir canın evine dönmesine yardım et.")
                        .setFont(regularFont)
                        .setFontSize(7.8f)
                        .setFontColor(MUTED_TEXT_COLOR)
                        .setMargin(0))
                .setPadding(0)
                .setBorder(Border.NO_BORDER));

        footer.addCell(new Cell()
                .add(new Paragraph("patimati.me")
                        .setFont(boldFont)
                        .setFontSize(8.5f)
                        .setFontColor(BRAND_ORANGE)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMargin(0))
                .setPadding(0)
                .setBorder(Border.NO_BORDER));

        document.add(footer);
    }

    /**
     * Fotoğrafları sırayla dener. Bir nesne okunamazsa afiş tamamen bozulmaz;
     * sıradaki fotoğrafa veya yer tutucuya geçer.
     */
    private Image loadFirstAvailablePhoto(Ad ad) {
        if (ad.getPhotoUrls() == null || ad.getPhotoUrls().isEmpty()) {
            return null;
        }

        for (String photoReference : ad.getPhotoUrls()) {
            if (!hasText(photoReference)) {
                continue;
            }

            try {
                byte[] imageBytes = imageStorageService.readImage(photoReference);
                if (imageBytes != null && imageBytes.length > 0) {
                    return new Image(ImageDataFactory.create(imageBytes));
                }
            } catch (RuntimeException exception) {
                log.warn(
                        "Afiş fotoğrafı okunamadı; sıradaki fotoğraf denenecek. adId={}, reference={}, hata={}",
                        ad.getId(),
                        photoReference,
                        exception.getMessage()
                );
            }
        }
        return null;
    }

    private String posterDetailUrl(Long adId) {
        String baseUrl = hasText(frontendUrl)
                ? frontendUrl.replaceAll("/+$", "")
                : "http://localhost:5173";
        return baseUrl + "/ads/" + adId;
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // Dört modüllük sessiz alan, yazıcı ve telefon kameralarında okumayı kolaylaştırır.
        hints.put(EncodeHintType.MARGIN, 4);

        BitMatrix matrix = new QRCodeWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints
        );

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "PNG", output);
            return output.toByteArray();
        }
    }

    private PdfFont loadTurkishFont(boolean bold) {
        String fontResourceName = bold ? "fonts/Roboto-Bold.ttf" : "fonts/Roboto-Regular.ttf";
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(fontResourceName);
            if (resource.exists()) {
                try (InputStream input = resource.getInputStream()) {
                    return PdfFontFactory.createFont(input.readAllBytes(), PdfEncodings.IDENTITY_H);
                }
            }
        } catch (Exception exception) {
            log.warn("Classpath font okunamadı: {}", fontResourceName, exception);
        }

        String[] possibleFontPaths = bold ? new String[]{
                "C:/Windows/Fonts/arialbd.ttf",
                "C:/Windows/Fonts/calibrib.ttf",
                "C:/Windows/Fonts/segoeuib.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                "/usr/share/fonts/truetype/freefont/FreeSansBold.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
                "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
        } : new String[]{
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/calibri.ttf",
                "C:/Windows/Fonts/segoeui.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf"
        };

        for (String fontPath : possibleFontPaths) {
            if (new File(fontPath).exists()) {
                try {
                    return PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
                } catch (Exception exception) {
                    log.warn("Font yüklenemedi: {}", fontPath, exception);
                }
            }
        }

        if (bold) {
            return loadTurkishFont(false);
        }

        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA);
        } catch (Exception exception) {
            throw new RuntimeException("PdfFont oluşturulamadı", exception);
        }
    }

    /**
     * Web sitesindeki marka yazısının font-family değeri Inter'dır. PDF'in
     * her ortamda aynı görünmesi için Inter ExtraBold proje kaynağından gömülür.
     */
    private PdfFont loadBrandFont(PdfFont fallbackFont) {
        String fontResourceName = "fonts/Inter-ExtraBold.ttf";
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(fontResourceName);
            if (resource.exists()) {
                try (InputStream input = resource.getInputStream()) {
                    return PdfFontFactory.createFont(input.readAllBytes(), PdfEncodings.IDENTITY_H);
                }
            }
        } catch (Exception exception) {
            log.warn("Inter marka fontu yüklenemedi; kalın PDF fontuna dönülecek.", exception);
        }
        return fallbackFont;
    }

    private static PosterTheme themeFor(Ad.AdType adType) {
        if (adType == Ad.AdType.FOUND) {
            return new PosterTheme(
                    new DeviceRgb(37, 99, 235),
                    new DeviceRgb(239, 246, 255),
                    new DeviceRgb(191, 219, 254),
                    "BULUNDU İLANI",
                    "SAHİBİNİ BULALIM",
                    "Bu dostun ailesine ulaşmasına yardımcı olun.",
                    "Bulunan evcil hayvan",
                    "SAHİBİNİ TANIYORSANIZ",
                    "Hemen haber verin",
                    "İlan sahibiyle güvenli biçimde iletişime geçin."
            );
        }

        if (adType == Ad.AdType.ADOPTION) {
            return new PosterTheme(
                    new DeviceRgb(22, 163, 74),
                    new DeviceRgb(240, 253, 244),
                    new DeviceRgb(187, 247, 208),
                    "YUVA İLANI",
                    "SICAK BİR YUVA BULALIM",
                    "Onun yeni ailesine ulaşmasına yardımcı olun.",
                    "Yeni yuvasını arıyor",
                    "YUVA OLMAK İSTİYORSANIZ",
                    "İlanı inceleyin",
                    "Detayları öğrenmek için güvenli biçimde iletişime geçin."
            );
        }

        return new PosterTheme(
                new DeviceRgb(239, 68, 68),
                new DeviceRgb(254, 242, 242),
                new DeviceRgb(254, 202, 202),
                "KAYIP İLANI",
                "BİRLİKTE BULALIM",
                "Bir konum bilgisi onu evine yaklaştırabilir.",
                "Kayıp evcil hayvan",
                "GÖRDÜYSENİZ",
                "Hemen haber verin",
                "Konumu ve zamanı paylaşmanız çok değerli."
        );
    }

    private static String locationText(Ad ad) {
        String city = hasText(ad.getCity()) ? ad.getCity().trim() : "";
        String district = hasText(ad.getDistrict()) ? ad.getDistrict().trim() : "";

        if (!city.isEmpty() && !district.isEmpty()) {
            return district + " / " + city;
        }
        if (!district.isEmpty()) {
            return district;
        }
        if (!city.isEmpty()) {
            return city;
        }
        return "Konum ilan detayında";
    }

    private static String lostDateText(Ad ad) {
        return ad.getLostDate() != null ? DATE_FORMAT.format(ad.getLostDate()) : "Belirtilmemiş";
    }

    private static String speciesAndBreedText(Ad ad) {
        String species = ad.getSpecies() != null ? turkceTur(ad.getSpecies()) : "Belirtilmemiş";
        String breed = turkceIrk(ad.getBreed());
        return "-".equals(breed) ? species : species + " / " + breed;
    }

    private static String genderAndAgeText(Ad ad) {
        String gender = ad.getGender() != null ? turkceCinsiyet(ad.getGender()) : "Belirtilmemiş";
        String age = ad.getAgeGroup() != null ? turkceYasGrubu(ad.getAgeGroup()) : "Belirtilmemiş";
        return gender + " / " + age;
    }

    private static String colorsText(Ad ad) {
        if (ad.getColors() == null || ad.getColors().isEmpty()) {
            return "Renk belirtilmemiş";
        }
        return ad.getColors().stream()
                .map(PosterServiceImpl::turkceRenk)
                .collect(Collectors.joining(", "));
    }

    private static String contactName(User owner) {
        String firstName = hasText(owner.getFirstName()) ? owner.getFirstName().trim() : "";
        String lastName = hasText(owner.getLastName()) ? owner.getLastName().trim() : "";
        return (firstName + " " + lastName).trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String truncate(String value, int maxLength, String fallback) {
        if (!hasText(value)) {
            return fallback;
        }

        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength - 3).trim() + "...";
    }

    private static String turkceTur(Species species) {
        return switch (species) {
            case CAT -> "Kedi";
            case DOG -> "Köpek";
            default -> "Belirtilmemiş";
        };
    }

    private static String turkceCinsiyet(PetGender gender) {
        return switch (gender) {
            case FEMALE -> "Dişi";
            case MALE -> "Erkek";
            default -> "Belirtilmemiş";
        };
    }

    private static String turkceYasGrubu(AgeGroup ageGroup) {
        return switch (ageGroup) {
            case BABY -> "Yavru";
            case YOUNG -> "Genç";
            case ADULT -> "Yetişkin";
            case SENIOR -> "Yaşlı";
            default -> "Belirtilmemiş";
        };
    }

    private static String turkceRenk(PetColor color) {
        return switch (color) {
            case BLACK -> "Siyah";
            case WHITE -> "Beyaz";
            case GRAY -> "Gri";
            case BROWN -> "Kahverengi";
            case ORANGE -> "Turuncu";
            case CREAM -> "Krem";
            case GOLDEN -> "Altın";
            case BEIGE -> "Bej";
            default -> "Diğer";
        };
    }

    private static String turkceIrk(String breed) {
        if (!hasText(breed)) {
            return "-";
        }
        if ("MIXED_OR_UNKNOWN".equals(breed) || "UNKNOWN".equals(breed)) {
            return "Cins belirtilmemiş";
        }
        return breed.trim();
    }

    private record PosterTheme(
            DeviceRgb statusAccent,
            DeviceRgb statusSoft,
            DeviceRgb statusBorder,
            String statusLabel,
            String eyebrow,
            String introSubtitle,
            String fallbackTitle,
            String ctaEyebrow,
            String ctaTitle,
            String ctaSubtitle
    ) {
    }
}
