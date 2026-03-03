-- liquibase formatted sql;

--Changeset vitor.botelho:creating-user-table
--comment: Criação da tabela 'users'
CREATE TABLE users (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username    VARCHAR(50) NOT NULL UNIQUE,
                       email       VARCHAR(255) NOT NULL UNIQUE,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       active      BOOLEAN NOT NULL DEFAULT true
);

--Changeset vitor.botelho:creating-decks-table
--comment: Criação da tabela 'decks' (groups of cards)
CREATE TABLE decks (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                       name            VARCHAR(255) NOT NULL,
                       description     TEXT,
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       archived        BOOLEAN NOT NULL DEFAULT false,
                       new_cards_limit INT NOT NULL DEFAULT 20,
                       review_limit    INT NOT NULL DEFAULT 200,
                       CONSTRAINT uq_deck_user_name UNIQUE (user_id, name)
);

--Changeset vitor.botelho:creating-cards-table
--comment: Criação da tabela 'cards'
CREATE TABLE cards (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       deck_id         UUID NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
                       front           TEXT NOT NULL,
                       back            TEXT NOT NULL,
                       extra           TEXT,
                       tags            TEXT[],
                       created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       archived        BOOLEAN NOT NULL DEFAULT false
);


--Changeset vitor.botelho:creating-card_states-table
--comment: Criação da tabela 'card_states' - Card Learning State (SM-2 algorithm state per user-card pair)
CREATE TABLE card_states (
                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             card_id         UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
                             user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- SM-2 core fields
                             ease_factor     NUMERIC(4,2) NOT NULL DEFAULT 2.50,  -- EF: starts at 2.5
                             interval_days   INT NOT NULL DEFAULT 0,              -- I: current interval in days
                             repetitions     INT NOT NULL DEFAULT 0,              -- n: number of successful reviews
    -- Scheduling
                             status          VARCHAR(20) NOT NULL DEFAULT 'NEW',  -- NEW | LEARNING | REVIEW | RELEARN
                             due_date        DATE NOT NULL DEFAULT CURRENT_DATE,
                             last_reviewed   TIMESTAMPTZ,
    -- Aggregate stats
                             total_reviews   INT NOT NULL DEFAULT 0,
                             lapse_count     INT NOT NULL DEFAULT 0,
    -- Optimistic locking
                             version         INT NOT NULL DEFAULT 0,
                             created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                             updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                             CONSTRAINT uq_card_state_user UNIQUE (card_id, user_id),
                             CONSTRAINT chk_ease_factor CHECK (ease_factor >= 1.3),
                             CONSTRAINT chk_interval CHECK (interval_days >= 0),
                             CONSTRAINT chk_repetitions CHECK (repetitions >= 0),
                             CONSTRAINT chk_status CHECK (status IN ('NEW','LEARNING','REVIEW','RELEARN'))
);

--Changeset vitor.botelho:creating-review_logs-table
--comment: Criação da tabela 'review log' - Review Log (immutable audit trail)
CREATE TABLE review_logs (
                             id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             card_state_id   UUID NOT NULL REFERENCES card_states(id),
                             user_id         UUID NOT NULL REFERENCES users(id),
                             card_id         UUID NOT NULL REFERENCES cards(id),
    -- Review data
                             quality         SMALLINT NOT NULL,   -- 0-5 SM-2 quality rating
                             ease_factor_before  NUMERIC(4,2) NOT NULL,
                             ease_factor_after   NUMERIC(4,2) NOT NULL,
                             interval_before INT NOT NULL,
                             interval_after  INT NOT NULL,
                             repetitions_before INT NOT NULL,
                             repetitions_after  INT NOT NULL,
                             status_before   VARCHAR(20) NOT NULL,
                             status_after    VARCHAR(20) NOT NULL,
    -- Timing
                             time_taken_ms   INT,  -- time user spent on card
                             reviewed_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                             CONSTRAINT chk_quality CHECK (quality BETWEEN 0 AND 5)
);

-- =====================================================
-- INDEXES for performance at scale
-- =====================================================

--Changeset vitor.botelho:add-index-idx_card_states_new
CREATE INDEX idx_card_states_new ON card_states (user_id, status, created_at)
    WHERE status = 'NEW';

--Changeset vitor.botelho:add-index-idx_cards_deck
CREATE INDEX idx_cards_deck ON cards (deck_id) WHERE NOT archived;

--Changeset vitor.botelho:add-index-idx_review_logs_user_date
CREATE INDEX idx_review_logs_user_date ON review_logs (user_id, reviewed_at DESC);

--Changeset vitor.botelho:add-index-idx_review_logs_card
CREATE INDEX idx_review_logs_card ON review_logs (card_id, reviewed_at DESC);

--Changeset vitor.botelho:add-index-idx_users_email
CREATE INDEX idx_users_email ON users (email);

--Changeset vitor.botelho:add-index-idx_decks_user
CREATE INDEX idx_decks_user ON decks (user_id) WHERE NOT archived;

--Changeset vitor.botelho:add-index-idx_cards_tags
CREATE INDEX idx_cards_tags ON cards USING GIN (tags);
