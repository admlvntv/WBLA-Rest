CREATE TABLE IF NOT EXISTS "version_history"
(
    "version"     bigint NOT NULL PRIMARY KEY,
    "description" text,
    "updated_at"  timestamp DEFAULT now()
);
CREATE INDEX IF NOT EXISTS "idx-version_history-version" ON version_history ("version");

DO
$V1$
    BEGIN
        IF NOT EXISTS(SELECT 1 FROM version_history WHERE version = 1) THEN
            CREATE TABLE IF NOT EXISTS customer(
                customer_id     text NOT NULL UNIQUE,
                customer_name   text NOT NULL
            );
            CREATE INDEX IF NOT EXISTS "idx-customer_id_customer" ON customer ("customer_id");

            CREATE TABLE IF NOT EXISTS customer_licenses(
                customer_id     text   NOT NULL UNIQUE,
                license_id      text   NOT NULL UNIQUE,

                UNIQUE(customer_id,license_id),
                CONSTRAINT fk_customer_customer_license FOREIGN KEY (customer_id) REFERENCES customer (customer_id) ON DELETE CASCADE
            );
            CREATE INDEX IF NOT EXISTS "idx-customer_id-customer_license" ON customer_licenses ("customer_id");

            CREATE TABLE IF NOT EXISTS license(
                license_id  text NOT NULL UNIQUE,
                expires     timestamp NOT NULL,
                used        boolean NOT NULL,

                CONSTRAINT fk_license_id_customer_license FOREIGN KEY (license_id) REFERENCES customer_licenses(license_id) ON DELETE CASCADE
            );
            CREATE INDEX IF NOT EXISTS "idx-license_id-license" ON license("license_id");

            INSERT INTO version_history(version, description) VALUES (1, 'Init DB');
        END IF;
    END;
$V1$;