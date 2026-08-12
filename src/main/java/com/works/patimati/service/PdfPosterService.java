package com.works.patimati.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.works.patimati.entity.Ad;
import com.works.patimati.exception.ResourceNotFoundException;
import com.works.patimati.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class PdfPosterService {

    private final AdRepository adRepository;
    private final QrCodeService qrCodeService;

    public byte[] generateAdPoster(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new ResourceNotFoundException("Afiş oluşturulacak ilan bulunamadı: " + adId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc);

            Paragraph header = new Paragraph("KAYIP HAYVAN İLANI")
                    .setFontSize(28)
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(header);

            Paragraph title = new Paragraph(ad.getTitle() != null ? ad.getTitle() : "Kayıp Aranıyor!")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(title);

            if (ad.getPhotoUrls() != null && !ad.getPhotoUrls().isEmpty()) {
                try {
                    String photoUrl = ad.getPhotoUrls().get(0);
                    ImageData imageData = ImageDataFactory.create(new URL(photoUrl));
                    Image img = new Image(imageData);
                    img.setMaxWidth(350);
                    img.setMaxHeight(300);
                    img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    img.setMarginBottom(15);
                    document.add(img);
                } catch (Exception e) {
                }
            }

            if (ad.getDescription() != null) {
                Paragraph desc = new Paragraph("Açıklama: " + ad.getDescription())
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(15);
                document.add(desc);
            }

            String ownerName = (ad.getUser() != null) ? ad.getUser().getFirstName() + " " + ad.getUser().getLastName() : "İlan Sahibi";
            String ownerPhone = (ad.getUser() != null && ad.getUser().getPhone() != null) ? ad.getUser().getPhone() : "İletişim Numarası Belirtilmemiş";

            Paragraph contactInfo = new Paragraph("İletişim: " + ownerName + " - " + ownerPhone)
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(ColorConstants.BLUE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(contactInfo);

            String targetUrl = "https://patimati.com/ads/" + ad.getId();
            byte[] qrBytes = qrCodeService.generateQrCodeImage(targetUrl, 150, 150);
            ImageData qrData = ImageDataFactory.create(qrBytes);
            Image qrImage = new Image(qrData);
            qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            document.add(qrImage);

            Paragraph qrNotice = new Paragraph("Detaylar için QR Kodu Tara")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(qrNotice);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF Afiş üretilirken bir hata oluştu: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}