CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE financial_profile (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    profile_type VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_financial_profile_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE category (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    color VARCHAR(20),
    financial_profile_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_category_financial_profile FOREIGN KEY (financial_profile_id) REFERENCES financial_profile(id),
    CONSTRAINT uk_category_name_profile UNIQUE (name, financial_profile_id)
);

CREATE TABLE supplier (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    document VARCHAR(20),
    email VARCHAR(150),
    phone VARCHAR(20),
    notes TEXT,
    financial_profile_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_supplier_financial_profile FOREIGN KEY (financial_profile_id) REFERENCES financial_profile(id)
);

CREATE TABLE recurrence (
    id UUID PRIMARY KEY,
    description VARCHAR(150) NOT NULL,
    default_amount NUMERIC(12,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    due_day INTEGER,
    active BOOLEAN NOT NULL,
    financial_profile_id UUID NOT NULL,
    category_id UUID NOT NULL,
    supplier_id UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_recurrence_financial_profile FOREIGN KEY (financial_profile_id) REFERENCES financial_profile(id),
    CONSTRAINT fk_recurrence_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_recurrence_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);

CREATE TABLE payable_account (
    id UUID PRIMARY KEY,
    description VARCHAR(150) NOT NULL,
    original_amount NUMERIC(12,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes TEXT,
    financial_profile_id UUID NOT NULL,
    category_id UUID NOT NULL,
    supplier_id UUID,
    recurrence_id UUID,
    issue_date DATE,
    competence_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payable_account_financial_profile FOREIGN KEY (financial_profile_id) REFERENCES financial_profile(id),
    CONSTRAINT fk_payable_account_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_payable_account_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id),
    CONSTRAINT fk_payable_account_recurrence FOREIGN KEY (recurrence_id) REFERENCES recurrence(id)
);

CREATE TABLE payment (
    id UUID PRIMARY KEY,
    payable_account_id UUID NOT NULL UNIQUE,
    paid_amount NUMERIC(12,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    interest_amount NUMERIC(12,2),
    discount_amount NUMERIC(12,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payment_payable_account FOREIGN KEY (payable_account_id) REFERENCES payable_account(id)
);

CREATE TABLE notification (
    id UUID PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(30) NOT NULL,
    is_read BOOLEAN NOT NULL,
    user_id UUID NOT NULL,
    payable_account_id UUID,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notification_payable_account FOREIGN KEY (payable_account_id) REFERENCES payable_account(id)
);

CREATE TABLE attachment (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    payable_account_id UUID,
    payment_id UUID,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachment_payable_account FOREIGN KEY (payable_account_id) REFERENCES payable_account(id),
    CONSTRAINT fk_attachment_payment FOREIGN KEY (payment_id) REFERENCES payment(id)
);
