package com.datech.mvp.service;

import com.datech.mvp.model.Client;
import com.datech.mvp.model.Invoice;
import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.ClientRepository;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoicePdfService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;

    public InvoicePdfService(ClientRepository clientRepository,
                             ProjectRepository projectRepository,
                             TimeEntryRepository timeEntryRepository) {
        this.clientRepository = clientRepository;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
    }

    public byte[] generateInvoicePdf(Long clientId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
        if (endDate.toEpochDay() - startDate.toEpochDay() > 365) {
            throw new IllegalArgumentException("Date range cannot exceed 365 days");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));

        List<Project> projects = projectRepository.findByClientId(clientId);

        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Project project : projects) {
            List<TimeEntry> entries = timeEntryRepository.findByProjectId(project.getId());
            for (TimeEntry entry : entries) {
                if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(endDate)) {
                    totalHours = totalHours.add(entry.getHours());
                    totalAmount = totalAmount.add(
                            entry.getHours().multiply(project.getHourlyRate())
                    );
                }
            }
        }

        totalHours = totalHours.setScale(2, RoundingMode.HALF_UP);
        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("INVOICE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Client: " + client.getName()));
            document.add(new Paragraph("Date Range: " + startDate + " to " + endDate));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Hours: " + totalHours));
            document.add(new Paragraph("Total Amount: " + totalAmount));

            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}