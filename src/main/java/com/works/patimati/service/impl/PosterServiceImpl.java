package com.works.patimati.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.works.patimati.entity.Ad;
import com.works.patimati.entity.User;
import com.works.patimati.entity.enums.AgeGroup;
import com.works.patimati.entity.enums.PetColor;
import com.works.patimati.entity.enums.PetGender;
import com.works.patimati.entity.enums.Species;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.PosterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosterServiceImpl implements PosterService {

    private static final Logger log = LoggerFactory.getLogger(PosterServiceImpl.class);
    private final AdRepository adRepository;

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

        boolean isOwner = (requestingUserEmail != null && ad.getUser() != null
                && requestingUserEmail.equalsIgnoreCase(ad.getUser().getEmail()));

        if (!isOwner && !Boolean.TRUE.equals(ad.getIsPosterAllowed())) {
            throw new org.springframework.security.access.AccessDeniedException("Afiş oluşturma kapalıdır");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);

            PdfFont turkishFont = loadTurkishFont(false);
            PdfFont turkishBoldFont = loadTurkishFont(true);
            if (turkishFont != null) {
                doc.setFont(turkishFont);
            }

            // Başlık Alanı
            DeviceRgb headerColor = switch (ad.getAdType()) {
                case LOST -> new DeviceRgb(220, 53, 69); // Kırmızı (Kayıp)
                case HELP -> new DeviceRgb(8, 145, 178); // Camgöbeği (Yardım)
                case FOUND, ADOPTION -> new DeviceRgb(40, 167, 69); // Yeşil (Bulundu/Sahiplendirme)
            };

            String bannerText = switch (ad.getAdType()) {
                case LOST -> "KAYIP EVCİL HAYVAN AFİŞİ";
                case ADOPTION -> "SAHİPLENDİRME AFİŞİ";
                case HELP -> "YARDIMA MUHTAÇ HAYVAN AFİŞİ";
                case FOUND -> "EVCİL HAYVAN İLANI";
            };

            Paragraph header = new Paragraph("PATİMATİ")
                    .setFontSize(28)
                    .setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER);
            if (turkishBoldFont != null) {
                header.setFont(turkishBoldFont);
            } else if (turkishFont != null) {
                header.setFont(turkishFont);
            }
            doc.add(header);

            Paragraph banner = new Paragraph(bannerText)
                    .setFontSize(20)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            if (turkishBoldFont != null) {
                banner.setFont(turkishBoldFont);
            } else if (turkishFont != null) {
                banner.setFont(turkishFont);
            }
            doc.add(banner);

            doc.add(new Paragraph("\n"));

            // İlan Başlığı
            Paragraph title = new Paragraph(ad.getTitle() != null ? ad.getTitle() : "Detaylar")
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            if (turkishBoldFont != null) {
                title.setFont(turkishBoldFont);
            } else if (turkishFont != null) {
                title.setFont(turkishFont);
            }
            doc.add(title);

            doc.add(new Paragraph("\n"));

            // İlan Detay Tablosu
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            table.setWidth(UnitValue.createPercentValue(100));
            if (turkishFont != null) {
                table.setFont(turkishFont);
            }

            addTableRow(table, "İlan Tipi:", ad.getAdType() != null ? turkceAdTipi(ad.getAdType()) : "-", turkishFont, turkishBoldFont);
            addTableRow(table, "Tür:", ad.getSpecies() != null ? turkceTur(ad.getSpecies()) : "-", turkishFont, turkishBoldFont);
            addTableRow(table, "Irk:", turkceIrk(ad.getBreed()), turkishFont, turkishBoldFont);
            addTableRow(table, "Cinsiyet:", ad.getGender() != null ? turkceCinsiyet(ad.getGender()) : "-", turkishFont, turkishBoldFont);
            addTableRow(table, "Yaş Grubu:", ad.getAgeGroup() != null ? turkceYasGrubu(ad.getAgeGroup()) : "-", turkishFont, turkishBoldFont);

            if (ad.getColors() != null && !ad.getColors().isEmpty()) {
                String colorStr = ad.getColors().stream().map(PosterServiceImpl::turkceRenk).collect(Collectors.joining(", "));
                addTableRow(table, "Renkler:", colorStr, turkishFont, turkishBoldFont);
            }

            if (ad.getMicrochipNumber() != null && !ad.getMicrochipNumber().isBlank()) {
                addTableRow(table, "Mikroçip No:", ad.getMicrochipNumber(), turkishFont, turkishBoldFont);
            }

            if (ad.getDistinctiveMarks() != null && !ad.getDistinctiveMarks().isBlank()) {
                addTableRow(table, "Belirgin İzler:", ad.getDistinctiveMarks(), turkishFont, turkishBoldFont);
            }

            if (ad.getDescription() != null && !ad.getDescription().isBlank()) {
                addTableRow(table, "Açıklama:", ad.getDescription(), turkishFont, turkishBoldFont);
            }

            // İletişim Bilgileri (İlan Sahibinin İzinlerine Bağlı)
            User owner = ad.getUser();
            if (owner != null) {
                String contactName = (owner.getFirstName() + " " + owner.getLastName()).trim();
                if (!contactName.isBlank()) {
                    addTableRow(table, "İletişim Kişisi:", contactName, turkishFont, turkishBoldFont);
                }

                if (Boolean.TRUE.equals(ad.getShowPhoneOnPoster()) && owner.getPhone() != null && !owner.getPhone().isBlank()) {
                    addTableRow(table, "Telefon:", owner.getPhone(), turkishFont, turkishBoldFont);
                }

                if (Boolean.TRUE.equals(ad.getShowEmailOnPoster()) && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                    addTableRow(table, "E-posta:", owner.getEmail(), turkishFont, turkishBoldFont);
                }
            }

            doc.add(table);
            doc.add(new Paragraph("\n"));

            // Dinamik Frontend URL ile QR Kod Oluşturma
            try {
                String baseUrl = (frontendUrl != null && !frontendUrl.isBlank())
                        ? frontendUrl.replaceAll("/+$", "")
                        : "http://localhost:5173";
                String qrContent = baseUrl + "/ads/" + ad.getId();

                byte[] qrCodeBytes = generateQrCodeImage(qrContent, 160, 160);
                Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes));
                qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

                Paragraph qrLabel = new Paragraph("İlan detaylarını görüntülemek için QR kodu okutun:")
                        .setFontSize(10)
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER);
                if (turkishFont != null) {
                    qrLabel.setFont(turkishFont);
                }

                doc.add(qrLabel);
                doc.add(qrImage);
            } catch (Exception e) {
                log.warn("QR kod üretilemedi: {}", e.getMessage());
            }

            // Alt Bilgi (Footer)
            Paragraph footer = new Paragraph("PatiMati - Evcil Hayvan Bulma ve Sahiplendirme Platformu")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            if (turkishFont != null) {
                footer.setFont(turkishFont);
            }
            doc.add(footer);

            doc.close();
            log.info("İlan afişi PDF başarıyla üretildi. adId={}", adId);
            return baos.toByteArray();
        } catch (org.springframework.security.access.AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("İlan afişi PDF üretilirken hata oluştu. adId={}", adId, e);
            throw new RuntimeException("Afiş PDF üretilemedi: " + e.getMessage(), e);
        }
    }

    private PdfFont loadTurkishFont() {
        return loadTurkishFont(false);
    }

    private PdfFont loadTurkishFont(boolean bold) {
        String fontResourceName = bold ? "fonts/Roboto-Bold.ttf" : "fonts/Roboto-Regular.ttf";
        try {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(fontResourceName);
            if (resource.exists()) {
                byte[] fontBytes = resource.getInputStream().readAllBytes();
                return PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);
            }
        } catch (Exception e) {
            log.warn("ClassPathResource font okunamadı: {}", fontResourceName, e);
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
                } catch (Exception e) {
                    log.warn("Font yüklenemedi: {}", fontPath, e);
                }
            }
        }

        String classpathFont = bold ? "/fonts/FreeSansBold.ttf" : "/fonts/FreeSans.ttf";
        try (InputStream is = getClass().getResourceAsStream(classpathFont)) {
            if (is != null) {
                byte[] fontBytes = is.readAllBytes();
                return PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);
            }
        } catch (Exception e) {
            log.warn("Classpath font yüklenemedi: {}", classpathFont, e);
        }

        if (bold) {
            return loadTurkishFont(false);
        }

        try {
            return PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont(StandardFonts.HELVETICA);
            } catch (Exception ex) {
                throw new RuntimeException("PdfFont oluşturulamadı", ex);
            }
        }
    }

    private void addTableRow(Table table, String key, String value, PdfFont regularFont, PdfFont boldFont) {
        Paragraph pKey = new Paragraph(key);
        if (boldFont != null) {
            pKey.setFont(boldFont);
        } else if (regularFont != null) {
            pKey.setFont(regularFont);
        }

        Paragraph pVal = new Paragraph(value != null ? value : "-");
        if (regularFont != null) {
            pVal.setFont(regularFont);
        }

        Cell cellKey = new Cell().add(pKey).setPadding(5);
        Cell cellVal = new Cell().add(pVal).setPadding(5);

        if (boldFont != null) {
            cellKey.setFont(boldFont);
        } else if (regularFont != null) {
            cellKey.setFont(regularFont);
        }
        if (regularFont != null) {
            cellVal.setFont(regularFont);
        }

        table.addCell(cellKey);
        table.addCell(cellVal);
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "PNG", baos);
            return baos.toByteArray();
        }
    }

    private static String turkceAdTipi(Ad.AdType adType) {
        switch (adType) {
            case LOST: return "Kayıp";
            case FOUND: return "Bulundu";
            case ADOPTION: return "Sahiplendirme";
            case HELP: return "Yardım";
            default: return "İlan";
        }
    }

    private static String turkceTur(Species species) {
        switch (species) {
            case CAT: return "Kedi";
            case DOG: return "Köpek";
            default: return "Belirtilmemiş";
        }
    }

    private static String turkceCinsiyet(PetGender gender) {
        switch (gender) {
            case FEMALE: return "Dişi";
            case MALE: return "Erkek";
            default: return "Belirtilmemiş";
        }
    }

    private static String turkceYasGrubu(AgeGroup ageGroup) {
        switch (ageGroup) {
            case BABY: return "Yavru";
            case YOUNG: return "Genç";
            case ADULT: return "Yetişkin";
            case SENIOR: return "Yaşlı";
            default: return "Belirtilmemiş";
        }
    }

    private static String turkceRenk(PetColor color) {
        switch (color) {
            case BLACK: return "Siyah";
            case WHITE: return "Beyaz";
            case GRAY: return "Gri";
            case BROWN: return "Kahverengi";
            case ORANGE: return "Turuncu";
            case CREAM: return "Krem";
            case GOLDEN: return "Altın";
            case BEIGE: return "Bej";
            default: return "Diğer";
        }
    }

    private static String turkceIrk(String breed) {
        if (breed == null || breed.isBlank()) {
            return "-";
        }
        if ("MIXED_OR_UNKNOWN".equals(breed) || "UNKNOWN".equals(breed)) {
            return "Cins belirtilmemiş";
        }
        return breed;
    }
}