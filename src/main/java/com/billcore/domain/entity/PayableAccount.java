package com.billcore.domain.entity;

import com.billcore.domain.enums.PayableAccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "payable_account")
public class PayableAccount {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayableAccountStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "financial_profile_id", nullable = false)
    private FinancialProfile financialProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_id")
    private Recurrence recurrence;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "competence_date")
    private LocalDate competenceDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "payableAccount")
    private Payment payment;

    @OneToMany(mappedBy = "payableAccount")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "payableAccount")
    private List<Attachment> attachments = new ArrayList<>();

    public PayableAccount() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public PayableAccountStatus getStatus() {
        return status;
    }

    public void setStatus(PayableAccountStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public FinancialProfile getFinancialProfile() {
        return financialProfile;
    }

    public void setFinancialProfile(FinancialProfile financialProfile) {
        this.financialProfile = financialProfile;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getCompetenceDate() {
        return competenceDate;
    }

    public void setCompetenceDate(LocalDate competenceDate) {
        this.competenceDate = competenceDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markAsPaid() {
        if (status != PayableAccountStatus.PENDING && status != PayableAccountStatus.OVERDUE) {
            throw new IllegalStateException("Only pending or overdue accounts can be paid");
        }
        this.status = PayableAccountStatus.PAID;
    }

    public void markAsOverdue() {
        if (status != PayableAccountStatus.PENDING) {
            throw new IllegalStateException("Only pending accounts can become overdue");
        }
        this.status = PayableAccountStatus.OVERDUE;
    }

    public void cancel() {
        if (status != PayableAccountStatus.PENDING) {
            throw new IllegalStateException("Only pending accounts can be canceled");
        }
        this.status = PayableAccountStatus.CANCELED;
    }

    public boolean isOverdue(LocalDate today) {
        return status == PayableAccountStatus.PENDING && dueDate != null && dueDate.isBefore(today);
    }
}
