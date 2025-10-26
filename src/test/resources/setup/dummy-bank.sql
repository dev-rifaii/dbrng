-- ==========================
-- BANK SCHEMA - POSTGRESQL
-- ==========================

-----------------------
-- Reference / lookup
-----------------------
CREATE TABLE currencies (
                            id BIGINT PRIMARY KEY,
                            code CHAR(3) NOT NULL UNIQUE,
                            name TEXT NOT NULL,
                            decimal_places SMALLINT NOT NULL CHECK (decimal_places >= 0 AND decimal_places <= 6)
);

CREATE TABLE branches (
                          id BIGINT PRIMARY KEY,
                          code VARCHAR(10) UNIQUE NOT NULL,
                          name TEXT NOT NULL,
                          address TEXT,
                          phone TEXT,
                          country CHAR(2)
);

CREATE TABLE transaction_types (
                                   id BIGINT PRIMARY KEY,
                                   code VARCHAR(30) UNIQUE NOT NULL,
                                   description TEXT
);

CREATE TABLE roles (
                       id BIGINT PRIMARY KEY,
                       name TEXT UNIQUE NOT NULL,
                       description TEXT
);

-----------------------
-- Users / employees
-----------------------
CREATE TABLE users (
                       id BIGINT PRIMARY KEY,
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email TEXT UNIQUE NOT NULL,
                       full_name TEXT NOT NULL,
                       dob DATE,
                       phone_numbers TEXT[],
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       metadata JSONB DEFAULT '{}',
                       status VARCHAR(20) NOT NULL DEFAULT 'active'
);

CREATE TABLE employees (
                           id BIGINT PRIMARY KEY,
                           user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           employee_no VARCHAR(30) UNIQUE,
                           branch_id BIGINT REFERENCES branches(id) ON DELETE SET NULL,
                           hire_date DATE,
                           terminated_at DATE,
                           roles TEXT[] DEFAULT ARRAY['teller']
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
                            assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            PRIMARY KEY (user_id, role_id)
);

-----------------------
-- Beneficiaries
-----------------------
CREATE TABLE beneficiaries (
                               id BIGINT PRIMARY KEY,
                               owner_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               name TEXT NOT NULL,
                               account_number VARCHAR(64),
                               bank_name TEXT,
                               currency_id BIGINT REFERENCES currencies(id),
                               metadata JSONB DEFAULT '{}',
                               added_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-----------------------
-- Accounts & limits
-----------------------
CREATE TABLE accounts (
                          id BIGINT PRIMARY KEY,
                          account_number VARCHAR(34) UNIQUE NOT NULL,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                          branch_id BIGINT REFERENCES branches(id) ON DELETE SET NULL,
                          currency_id BIGINT NOT NULL REFERENCES currencies(id),
                          status VARCHAR(20) NOT NULL DEFAULT 'active',
                          opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          closed_at TIMESTAMPTZ,
                          lifecycle TSTZRANGE NOT NULL DEFAULT tstzrange(now(), NULL),
                          properties JSONB DEFAULT '{}'
);
CREATE INDEX ON accounts (user_id);
CREATE INDEX ON accounts (account_number);

CREATE TABLE account_balances (
                                  id BIGSERIAL PRIMARY KEY,
                                  account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                                  recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  balance NUMERIC(20,4) NOT NULL,
                                  available NUMERIC(20,4) NOT NULL,
                                  reserved NUMERIC(20,4) NOT NULL DEFAULT 0,
                                  note TEXT
);
CREATE INDEX ON account_balances (account_id, recorded_at DESC);

CREATE TABLE account_limits (
                                id BIGINT PRIMARY KEY,
                                account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                                limit_type VARCHAR(50) NOT NULL,
                                amount_range NUMRANGE,
                                tx_count_range INT4RANGE,
                                valid_range TSTZRANGE,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-----------------------
-- Transactions & payments
-----------------------
CREATE TABLE transactions (
                              id BIGINT PRIMARY KEY,
                              reference VARCHAR(80) UNIQUE,
                              account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE RESTRICT,
                              txn_type_id BIGINT REFERENCES transaction_types(id),
                              amount NUMERIC(20,4) NOT NULL,
                              currency_id BIGINT NOT NULL REFERENCES currencies(id),
                              status VARCHAR(20) NOT NULL DEFAULT 'pending',
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                              settled_at TIMESTAMPTZ,
                              period TSTZRANGE,
                              CHECK (amount <> 0),
                              metadata JSONB DEFAULT '{}'
);
CREATE INDEX ON transactions (account_id, created_at DESC);
CREATE INDEX ON transactions (status);

CREATE TABLE payments (
                          id BIGINT PRIMARY KEY,
                          transaction_id BIGINT UNIQUE REFERENCES transactions(id) ON DELETE CASCADE,
                          from_account BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
                          to_beneficiary_id BIGINT REFERENCES beneficiaries(id) ON DELETE SET NULL,
                          to_account_number VARCHAR(64),
                          performed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
                          initiated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          executed_at TIMESTAMPTZ,
                          fee NUMERIC(14,4) DEFAULT 0,
                          fx_rate NUMERIC(18,10),
                          route TEXT,
                          memo TEXT
);
CREATE INDEX ON payments (performed_by);

CREATE TABLE scheduled_payments (
                                    id BIGINT PRIMARY KEY,
                                    owner_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                    from_account BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                                    to_beneficiary_id BIGINT REFERENCES beneficiaries(id),
                                    schedule_cron TEXT,
                                    next_run_at TIMESTAMPTZ,
                                    last_run_at TIMESTAMPTZ,
                                    enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                    metadata JSONB DEFAULT '{}'
);

CREATE TABLE payment_methods (
                                 id BIGINT PRIMARY KEY,
                                 user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 type VARCHAR(30) NOT NULL,
                                 details JSONB NOT NULL,
                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 is_primary BOOLEAN NOT NULL DEFAULT FALSE
);

-----------------------
-- Cards
-----------------------
CREATE TABLE cards (
                       id BIGINT PRIMARY KEY,
                       account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                       card_pan_hash BYTEA NOT NULL,
                       display_pan VARCHAR(19),
                       holder_name TEXT,
                       expiry_date DATE,
                       cvv_hash BYTEA,
                       status VARCHAR(20) NOT NULL DEFAULT 'active',
                       issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                       blocked_at TIMESTAMPTZ
);
CREATE INDEX ON cards (account_id);
CREATE INDEX ON cards (display_pan);

CREATE TABLE card_transactions (
                                   id BIGINT PRIMARY KEY,
                                   card_id BIGINT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                                   txn_id BIGINT REFERENCES transactions(id),
                                   posted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   merchant TEXT,
                                   amount NUMERIC(20,4) NOT NULL,
                                   currency_id BIGINT NOT NULL REFERENCES currencies(id),
                                   raw_merchant_data JSONB
);

-----------------------
-- Deposits, loans, contracts
-----------------------
CREATE TABLE deposits (
                          id BIGINT PRIMARY KEY,
                          account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                          amount NUMERIC(20,4) NOT NULL CHECK (amount > 0),
                          received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          source TEXT,
                          receipt BYTEA
);

CREATE TABLE loans (
                       id BIGINT PRIMARY KEY,
                       account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                       principal NUMERIC(20,4) NOT NULL,
                       outstanding NUMERIC(20,4) NOT NULL,
                       interest_rate NUMERIC(6,5) NOT NULL,
                       term_months INT NOT NULL CHECK (term_months > 0),
                       issued_at DATE NOT NULL,
                       maturity_date DATE,
                       status VARCHAR(20) NOT NULL DEFAULT 'active'
);

CREATE TABLE loan_payments (
                               id BIGINT PRIMARY KEY,
                               loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
                               payment_date DATE NOT NULL,
                               amount NUMERIC(20,4) NOT NULL,
                               principal_component NUMERIC(20,4) NOT NULL,
                               interest_component NUMERIC(20,4) NOT NULL
);

CREATE TABLE contracts (
                           id BIGINT PRIMARY KEY,
                           contract_no VARCHAR(60) UNIQUE NOT NULL,
                           party_a BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           party_b BIGINT REFERENCES users(id) ON DELETE SET NULL,
                           subject TEXT,
                           status VARCHAR(20) NOT NULL DEFAULT 'draft',
                           period TSTZRANGE,
                           signed_at TIMESTAMPTZ,
                           body TEXT,
                           attachments BIGINT[]
);

-----------------------
-- Statements, invoices, audit
-----------------------
CREATE TABLE statements (
                            id BIGINT PRIMARY KEY,
                            account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                            period TSTZRANGE NOT NULL,
                            generated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            statement_data JSONB
);

CREATE TABLE invoices (
                          id BIGINT PRIMARY KEY,
                          invoice_no VARCHAR(60) UNIQUE NOT NULL,
                          account_id BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
                          issued_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          due_date DATE,
                          total NUMERIC(20,4) NOT NULL,
                          items JSONB,
                          paid BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            actor_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
                            actor_employee_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
                            action VARCHAR(100) NOT NULL,
                            target_table VARCHAR(100),
                            target_id BIGINT,
                            occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            details JSONB
);

CREATE TABLE sessions (
                          id BIGINT PRIMARY KEY,
                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          last_seen TIMESTAMPTZ,
                          ip INET,
                          user_agent TEXT
);

CREATE TABLE devices (
                         id BIGINT PRIMARY KEY,
                         user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                         device_fingerprint TEXT UNIQUE,
                         registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         last_used TIMESTAMPTZ
);

CREATE TABLE notifications (
                               id BIGINT PRIMARY KEY,
                               user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                               type VARCHAR(60),
                               payload JSONB,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               read_at TIMESTAMPTZ
);

-----------------------
-- Exchange rates, attachments
-----------------------
CREATE TABLE exchange_rates (
                                id BIGINT PRIMARY KEY,
                                from_currency_id BIGINT NOT NULL REFERENCES currencies(id),
                                to_currency_id BIGINT NOT NULL REFERENCES currencies(id),
                                rate NUMERIC(25,12) NOT NULL,
                                valid_from TIMESTAMPTZ NOT NULL,
                                valid_to TIMESTAMPTZ,
                                UNIQUE (from_currency_id, to_currency_id, valid_from)
);

CREATE TABLE attachments (
                             id BIGINT PRIMARY KEY,
                             owner_id BIGINT,
                             owner_type VARCHAR(50),
                             filename TEXT,
                             content BYTEA,
                             content_type VARCHAR(100),
                             uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE kyc_documents (
                               id BIGINT PRIMARY KEY,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               doc_type VARCHAR(50) NOT NULL,
                               uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               data BYTEA,
                               verified_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
                               verified_at TIMESTAMPTZ
);
