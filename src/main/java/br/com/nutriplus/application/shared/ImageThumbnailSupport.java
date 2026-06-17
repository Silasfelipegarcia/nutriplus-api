package br.com.nutriplus.application.shared;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Iterator;

/**
 * Gera miniaturas (thumbnails) pequenas a partir de capas em base64 (data URL),
 * usando apenas o JDK (javax.imageio / java.awt) — sem infraestrutura externa.
 *
 * Estrategia: a capa cheia continua armazenada para a tela de detalhe; as LISTAS
 * passam a trafegar a miniatura (JPEG ~240px), que e ordens de grandeza menor que
 * o base64 original e mantem a experiencia visual.
 */
public final class ImageThumbnailSupport {

    private static final int TARGET_WIDTH = 240;
    private static final float JPEG_QUALITY = 0.7f;
    private static final String DATA_IMAGE_PREFIX = "data:image";

    private ImageThumbnailSupport() {
    }

    /**
     * Retorna uma miniatura JPEG (data URL base64) a partir de uma capa.
     * - Capa vazia ou URL externa (http): retorna "" (a lista usa a propria URL).
     * - Capa base64 valida: retorna a miniatura.
     * - Formato nao suportado / erro: retorna "" (a lista mostra placeholder).
     */
    public static String thumbnailFromPhoto(String coverUrlOrData) {
        if (coverUrlOrData == null) {
            return "";
        }
        String src = coverUrlOrData.strip();
        if (!src.regionMatches(true, 0, DATA_IMAGE_PREFIX, 0, DATA_IMAGE_PREFIX.length())) {
            return "";
        }
        int comma = src.indexOf(',');
        if (comma < 0) {
            return "";
        }
        String base64 = src.substring(comma + 1).replaceAll("\\s", "");
        try {
            byte[] raw = Base64.getDecoder().decode(base64);
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(raw));
            if (original == null) {
                return "";
            }
            original = applyExifOrientation(original, readExifOrientation(raw));
            int srcW = original.getWidth();
            int srcH = original.getHeight();
            if (srcW <= 0 || srcH <= 0) {
                return "";
            }
            int targetW = Math.min(TARGET_WIDTH, srcW);
            int targetH = Math.max(1, (int) Math.round(srcH * (targetW / (double) srcW)));

            BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetW, targetH);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(original, 0, 0, targetW, targetH, null);
            g.dispose();

            byte[] jpeg = encodeJpeg(scaled);
            if (jpeg.length == 0) {
                return "";
            }
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(jpeg);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Le tag Orientation (0x0112) do segmento EXIF em JPEG.
     * Valores 1-8 conforme spec EXIF; default 1 (sem rotacao).
     */
    static int readExifOrientation(byte[] jpeg) {
        if (jpeg == null || jpeg.length < 4) {
            return 1;
        }
        if ((jpeg[0] & 0xFF) != 0xFF || (jpeg[1] & 0xFF) != 0xD8) {
            return 1;
        }
        int offset = 2;
        while (offset + 4 < jpeg.length) {
            if ((jpeg[offset] & 0xFF) != 0xFF) {
                break;
            }
            int marker = jpeg[offset + 1] & 0xFF;
            if (marker == 0xE1) {
                int segLen = u16(jpeg, offset + 2);
                if (segLen >= 8 && offset + 2 + segLen <= jpeg.length) {
                    int exifStart = offset + 4;
                    if (exifStart + 6 <= jpeg.length
                            && jpeg[exifStart] == 'E'
                            && jpeg[exifStart + 1] == 'x'
                            && jpeg[exifStart + 2] == 'i'
                            && jpeg[exifStart + 3] == 'f') {
                        int orientation = parseExifOrientationTag(jpeg, exifStart + 6, offset + 2 + segLen);
                        if (orientation >= 1 && orientation <= 8) {
                            return orientation;
                        }
                    }
                }
                offset += 2 + Math.max(segLen, 2);
                continue;
            }
            if (marker == 0xDA || marker == 0xD9) {
                break;
            }
            if (marker >= 0xD0 && marker <= 0xD7) {
                offset += 2;
                continue;
            }
            int segLen = u16(jpeg, offset + 2);
            if (segLen < 2) {
                break;
            }
            offset += 2 + segLen;
        }
        return 1;
    }

    private static int u16(byte[] data, int index) {
        return ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
    }

    private static int u16le(byte[] data, int index) {
        return (data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8);
    }

    private static int parseExifOrientationTag(byte[] data, int tiffStart, int limit) {
        if (tiffStart + 8 > limit) {
            return 1;
        }
        boolean le = data[tiffStart] == 'I' && data[tiffStart + 1] == 'I';
        boolean be = data[tiffStart] == 'M' && data[tiffStart + 1] == 'M';
        if (!le && !be) {
            return 1;
        }
        int ifdOffset = readInt(data, tiffStart + 4, le);
        int ifd = tiffStart + ifdOffset;
        if (ifd < tiffStart || ifd + 2 > limit) {
            return 1;
        }
        int entries = readU16(data, ifd, le);
        int entryBase = ifd + 2;
        for (int i = 0; i < entries; i++) {
            int entry = entryBase + i * 12;
            if (entry + 12 > limit) {
                break;
            }
            int tag = readU16(data, entry, le);
            if (tag != 0x0112) {
                continue;
            }
            int value = readU16(data, entry + 8, le);
            return value >= 1 && value <= 8 ? value : 1;
        }
        return 1;
    }

    private static int readU16(byte[] data, int index, boolean le) {
        return le ? u16le(data, index) : u16(data, index);
    }

    private static int readInt(byte[] data, int index, boolean le) {
        if (le) {
            return (data[index] & 0xFF)
                    | ((data[index + 1] & 0xFF) << 8)
                    | ((data[index + 2] & 0xFF) << 16)
                    | ((data[index + 3] & 0xFF) << 24);
        }
        return ((data[index] & 0xFF) << 24)
                | ((data[index + 1] & 0xFF) << 16)
                | ((data[index + 2] & 0xFF) << 8)
                | (data[index + 3] & 0xFF);
    }

    static BufferedImage applyExifOrientation(BufferedImage src, int orientation) {
        if (orientation <= 1 || orientation > 8) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        AffineTransform tx = new AffineTransform();
        int newW = w;
        int newH = h;
        switch (orientation) {
            case 2 -> {
                tx.scale(-1.0, 1.0);
                tx.translate(-w, 0);
            }
            case 3 -> {
                tx.translate(w, h);
                tx.rotate(Math.PI);
            }
            case 4 -> {
                tx.scale(1.0, -1.0);
                tx.translate(0, -h);
            }
            case 5 -> {
                tx.rotate(-Math.PI / 2);
                tx.scale(-1.0, 1.0);
                newW = h;
                newH = w;
            }
            case 6 -> {
                tx.translate(h, 0);
                tx.rotate(Math.PI / 2);
                newW = h;
                newH = w;
            }
            case 7 -> {
                tx.rotate(-Math.PI / 2);
                tx.translate(-h, 0);
                tx.scale(-1.0, 1.0);
                newW = h;
                newH = w;
            }
            case 8 -> {
                tx.translate(0, w);
                tx.rotate(3 * Math.PI / 2);
                newW = h;
                newH = w;
            }
            default -> {
                return src;
            }
        }
        BufferedImage dst = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newW, newH);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, tx, null);
        g.dispose();
        return dst;
    }

    private static byte[] encodeJpeg(BufferedImage image) throws Exception {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            return new byte[0];
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }
}
