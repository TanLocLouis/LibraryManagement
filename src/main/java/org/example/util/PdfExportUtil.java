package org.example.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.model.Book;
import org.example.model.BorrowSlip;
import org.example.service.BorrowService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PdfExportUtil {
	private static final float FONT_SIZE = 12f;
	private static final float LEADING = 18f;

	private PdfExportUtil() {
	}

	public static void exportBorrowSlip(Path filePath, BorrowSlip slip, String readerName, List<Book> books) {
		List<String> lines = new ArrayList<>();
		lines.add("PHIEU MUON SACH");
		lines.add("Thoi gian xuat: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		lines.add("");
		lines.add("Ma phieu: " + safe(slip.getSlipId()));
		lines.add("Ma doc gia: " + safe(slip.getReaderId()));
		lines.add("Ten doc gia: " + safe(readerName));
		lines.add("Ngay muon: " + safe(slip.getBorrowDate()));
		lines.add("Ngay tra du kien: " + safe(slip.getDueDate()));
		lines.add("Danh sach sach muon:");
		lines.addAll(bookLines(books));
		writePdf(filePath, lines);
	}

	public static void exportReturnSlip(Path filePath,
										BorrowSlip slip,
										String readerName,
										List<Book> books,
										BorrowService.PenaltyBreakdown penaltyBreakdown) {
		List<String> lines = new ArrayList<>();
		lines.add("PHIEU TRA SACH");
		lines.add("Thoi gian xuat: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		lines.add("");
		lines.add("Ma phieu: " + safe(slip.getSlipId()));
		lines.add("Ma doc gia: " + safe(slip.getReaderId()));
		lines.add("Ten doc gia: " + safe(readerName));
		lines.add("Ngay muon: " + safe(slip.getBorrowDate()));
		lines.add("Ngay tra du kien: " + safe(slip.getDueDate()));
		lines.add("Ngay tra thuc te: " + safe(slip.getReturnDate()));
		lines.add("Danh sach ISBN da muon:");
		lines.addAll(bookLines(books));
		lines.add("ISBN bi mat: " + String.join(", ", slip.getLostIsbnList() == null ? List.of() : slip.getLostIsbnList()));
		lines.add("");
		lines.add("So ngay tre han: " + penaltyBreakdown.overdueDays());
		lines.add("Phat tre han: " + formatCurrency(penaltyBreakdown.overdueFine()));
		lines.add("Phat mat sach: " + formatCurrency(penaltyBreakdown.lostBookFine()));
		lines.add("Tong tien phat: " + formatCurrency(penaltyBreakdown.totalFine()));
		writePdf(filePath, lines);
	}

	private static List<String> bookLines(List<Book> books) {
		if (books == null || books.isEmpty()) {
			return List.of("- Khong co");
		}
		List<String> lines = new ArrayList<>();
		for (Book book : books) {
			lines.add("- " + safe(book.getIsbn()) + " | " + safe(book.getTitle()) + " | Gia: " + formatCurrency(book.getPrice()));
		}
		return lines;
	}

	private static void writePdf(Path filePath, List<String> lines) {
		if (filePath == null) {
			throw new IllegalArgumentException("Output file path is required");
		}
		try {
			Path parent = filePath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (PDDocument document = new PDDocument()) {
				PDPage page = new PDPage(PDRectangle.A4);
				document.addPage(page);
				PDFont font = loadFont(document);

				try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
					float y = page.getMediaBox().getHeight() - 60;
					contentStream.beginText();
					contentStream.setFont(font, FONT_SIZE);
					contentStream.newLineAtOffset(50, y);
					for (String line : lines) {
						contentStream.showText(safe(line));
						contentStream.newLineAtOffset(0, -LEADING);
					}
					contentStream.endText();
				}
				document.save(filePath.toFile());
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to export PDF", e);
		}
	}

	private static PDFont loadFont(PDDocument document) throws IOException {
		Path windowsArial = Path.of(System.getenv("WINDIR") == null ? "C:\\Windows" : System.getenv("WINDIR"), "Fonts", "arial.ttf");
		if (Files.exists(windowsArial)) {
			return PDType0Font.load(document, windowsArial.toFile());
		}
		return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	}

	private static String safe(String value) {
		return value == null ? "" : value;
	}

	private static String formatCurrency(long amount) {
		NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
		return formatter.format(amount) + " VND";
	}
}

