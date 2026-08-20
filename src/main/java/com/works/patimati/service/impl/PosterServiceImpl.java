package com.works.patimati.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
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
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import com.works.patimati.service.PosterService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosterServiceImpl implements PosterService {

    private static final Logger log = LoggerFactory.getLogger(PosterServiceImpl.class);
    private final AdRepository adRepository;

    @Transactional(readOnly = true)
    @Override
    public byte[] generateAdPosterPdf(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı ID: " + adId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);

            // Başlık Alanı
            DeviceRgb headerColor = (ad.getAdType() == Ad.AdType.LOST)
                    ? new DeviceRgb(220, 53, 69) // Kırmızı (Kayıp)
                    : new DeviceRgb(40, 167, 69); // Yeşil (Sahiplendirme)

            String bannerText = (ad.getAdType() == Ad.AdType.LOST)
                    ? "KAYIP EVCIL HAYVAN AFISI"
                    : (ad.getAdType() == Ad.AdType.ADOPTION ? "SAHIPLENDIRME AFISI" : "EVCIL HAYVAN ILANI");

            Paragraph header = new Paragraph("PATIMATI")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);

            Paragraph banner = new Paragraph(bannerText)
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(8);
            doc.add(banner);

            doc.add(new Paragraph("\n"));

            // İlan Başlığı
            Paragraph title = new Paragraph(ad.getTitle() != null ? ad.getTitle() : "Detaylar")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(title);

            doc.add(new Paragraph("\n"));

            // İlan Detay Tablosu
            Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableRow(table, "Ilan Tipi:", ad.getAdType() != null ? ad.getAdType().name() : "-");
            addTableRow(table, "Tur:", ad.getSpecies() != null ? ad.getSpecies().name() : "-");
            addTableRow(table, "Irk:", ad.getBreed() != null ? ad.getBreed() : "-");
            addTableRow(table, "Cinsiyet:", ad.getGender() != null ? ad.getGender().name() : "-");
            addTableRow(table, "Yas Grubu:", ad.getAgeGroup() != null ? ad.getAgeGroup().name() : "-");

            if (ad.getColors() != null && !ad.getColors().isEmpty()) {
                String colorStr = ad.getColors().stream().map(Enum::name).collect(Collectors.joining(", "));
                addTableRow(table, "Renkler:", colorStr);
            }

            if (ad.getMicrochipNumber() != null && !ad.getMicrochipNumber().isBlank()) {
                addTableRow(table, "Mikrocip No:", ad.getMicrochipNumber());
            }

            if (ad.getDistinctiveMarks() != null && !ad.getDistinctiveMarks().isBlank()) {
                addTableRow(table, "Belirgin Izler:", ad.getDistinctiveMarks());
            }

            if (ad.getDescription() != null && !ad.getDescription().isBlank()) {
                addTableRow(table, "Aciklama:", ad.getDescription());
            }

            // İletişim Bilgileri
            User owner = ad.getUser();
            if (owner != null) {
                String contactName = (owner.getFirstName() + " " + owner.getLastName()).trim();
                addTableRow(table, "Iletisim Kisisi:", contactName);
                if (owner.getPhone() != null && !owner.getPhone().isBlank()) {
                    addTableRow(table, "Telefon:", owner.getPhone());
                }
                addTableRow(table, "E-posta:", owner.getEmail());
            }

            doc.add(table);
            doc.add(new Paragraph("\n"));

            // QR Kod Oluşturma
            try {
                String qrContent = "https://patimati.com/ads/" + ad.getId();
                byte[] qrCodeBytes = generateQrCodeImage(qrContent, 150, 150);
                Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes));
                qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);

                Paragraph qrLabel = new Paragraph("Ilan detaylarini goruntulemek icin QR kodu okutun:")
                        .setFontSize(10)
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER);

                doc.add(qrLabel);
                doc.add(qrImage);
            } catch (Exception e) {
                log.warn("QR kod uretilemedi: {}", e.getMessage());
            }

            // Alt Bilgi (Footer)
            Paragraph footer = new Paragraph("PatiMati - Evcil Hayvan Bulma ve Sahiplendirme Platformu")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(footer);

            doc.close();
            log.info("İlan afişi PDF başarıyla üretildi. adId={}", adId);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("İlan afişi PDF üretilirken hata oluştu. adId={}", adId, e);
            throw new RuntimeException("Afiş PDF üretilemedi: " + e.getMessage(), e);
        }
    }

    private void addTableRow(Table table, String key, String value) {
        Cell cellKey = new Cell().add(new Paragraph(key).setBold()).setPadding(5);
        Cell cellVal = new Cell().add(new Paragraph(value != null ? value : "-")).setPadding(5);
        table.addCell(cellKey);
        table.addCell(cellVal);
    }

    private byte[] generateQrCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

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
}
