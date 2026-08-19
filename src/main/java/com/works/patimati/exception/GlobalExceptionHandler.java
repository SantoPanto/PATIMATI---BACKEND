package com.works.patimati.exception;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.works.patimati.storage.ImageStorageException;
import com.works.patimati.storage.InvalidImageException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                "Kaynak bulunamadı",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Geçersiz parametre",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.FORBIDDEN,
                "Erişim reddedildi",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "İşlem çakışması",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ProblemDetail> handleInvalidImage(
            InvalidImageException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Geçersiz fotoğraf",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ProblemDetail> handleStorageFailure(
            ImageStorageException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_GATEWAY,
                "Dosya depolama hatası",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleRequestValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        exception.getBindingResult()
                .getGlobalErrors()
                .forEach(error -> fieldErrors.putIfAbsent(
                        error.getObjectName(),
                        error.getDefaultMessage()
                ));

        ProblemDetail detail = createProblem(
                HttpStatus.BAD_REQUEST,
                "Doğrulama hatası",
                "Gönderilen istek verileri doğrulamadan geçemedi",
                request
        );
        detail.setProperty("validationErrors", fieldErrors);

        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        String detailMessage = "Gönderilen JSON verisi korrupt veya okunabilir biçimde değil.";

        Throwable mostSpecificCause = exception.getMostSpecificCause();
        if (mostSpecificCause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().stream()
                    .map(Reference::getFieldName)
                    .collect(Collectors.joining("."));
            String errorMessage = "Geçersiz değer: '" + invalidFormatException.getValue() + "'";
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                errorMessage = "'" + invalidFormatException.getValue() + "' geçerli bir enum değeri değil.";
            }
            if (!fieldName.isEmpty()) {
                fieldErrors.put(fieldName, errorMessage);
            }
            detailMessage = "JSON veri tipi veya alan formatı geçersiz: " + (fieldName.isEmpty() ? errorMessage : fieldName + " -> " + errorMessage);
        } else {
            Throwable current = exception;
            com.fasterxml.jackson.databind.JsonMappingException jsonMappingException = null;
            while (current != null) {
                if (current instanceof com.fasterxml.jackson.databind.JsonMappingException jme) {
                    jsonMappingException = jme;
                    break;
                }
                current = current.getCause();
            }

            String fieldName = "";
            if (jsonMappingException != null) {
                fieldName = jsonMappingException.getPath().stream()
                        .map(Reference::getFieldName)
                        .filter(name -> name != null && !name.isBlank())
                        .collect(Collectors.joining("."));
            }

            if (fieldName.isEmpty() && mostSpecificCause != null && mostSpecificCause.getMessage() != null) {
                if (mostSpecificCause.getMessage().contains("şikayet sebebi") || mostSpecificCause.getMessage().contains("ComplaintReason") || mostSpecificCause.getMessage().contains("reason")) {
                    fieldName = "reason";
                }
            }

            if (!fieldName.isEmpty() && mostSpecificCause != null && mostSpecificCause.getMessage() != null) {
                fieldErrors.put(fieldName, mostSpecificCause.getMessage());
            }

            if (mostSpecificCause != null && mostSpecificCause.getMessage() != null) {
                detailMessage = mostSpecificCause.getMessage();
            }
        }

        ProblemDetail detail = createProblem(
                HttpStatus.BAD_REQUEST,
                "Geçersiz JSON Verisi",
                detailMessage,
                request
        );
        if (!fieldErrors.isEmpty()) {
            detail.setProperty("validationErrors", fieldErrors);
        }

        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            MultipartException.class
    })
    public ResponseEntity<ProblemDetail> handleInvalidRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Geçersiz istek",
                "İstek parametreleri veya gönderilen veri biçimi geçersiz",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Dosya boyutu sınırı aşıldı",
                "Yüklenen fotoğrafların toplam boyutu izin verilen sınırı aşıyor",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        // Yeni Exception.class catch-all'ının (aşağıda) yakaladığı GERÇEK bir
        // regresyon: bu istisna için burada özel bir handler YOKTU, o yüzden
        // önceden Spring'in kendi DefaultHandlerExceptionResolver'ı devreye
        // girip doğru 405'i üretiyordu. Exception.class handler'ı
        // ExceptionHandlerExceptionResolver seviyesinde HER istisnayı
        // yakaladığı için (tip hiyerarşisi bakımdan Exception her şeyin
        // atasıdır), Spring'in kendi varsayılan çözümleyicisine sıra hiç
        // gelmiyordu -- AdControllerTest.shouldNotExposeRemovedTestEndpoint
        // 405 yerine 500 almaya başlayarak bunu yakaladı. Doğru semantiği
        // burada açıkça geri veriyoruz.
        return problem(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Desteklenmeyen HTTP metodu",
                exception.getMessage(),
                request
        );
    }

    // Son çare: yukarıdaki hiçbir spesifik handler'a uymayan her şey buraya
    // düşer. Spring, en spesifik eşleşen handler'ı @ExceptionHandler tip
    // hiyerarşisine göre seçer (dosyadaki sıralamadan bağımsız) -- bu yüzden
    // burada olması yukarıdaki handler'ların DAVRANIŞINI değiştirmez, yalnızca
    // önceden hiç yakalanmayan (ör. NullPointerException, beklenmeyen
    // RuntimeException) istekler artık çıplak 500 + boş gövde yerine tutarlı
    // bir ProblemDetail alır. İstemciye ham exception.getMessage() DÖNMEZ
    // (iç sınıf adı/SQL/stack detayı sızdırabilir) -- tam detay yalnızca
    // sunucu logunda, ERROR seviyesinde.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Beklenmeyen hata: {} {}", request.getMethod(), request.getRequestURI(), exception);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Beklenmeyen bir hata oluştu",
                "Sunucu tarafında beklenmeyen bir hata oluştu. Sorun devam ederse destek ile iletişime geçin.",
                request
        );
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(status)
                .body(createProblem(status, title, message, request));
    }

    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String message,
            HttpServletRequest request
    ) {
        ProblemDetail detail =
                ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(title);
        detail.setInstance(URI.create(request.getRequestURI()));
        return detail;
    }
}
