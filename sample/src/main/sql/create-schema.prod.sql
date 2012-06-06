
    create table sample.ACCOUNT (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ANNUAL_REVENUE decimal(19,2) check (ANNUAL_REVENUE>=0),
        ANNUAL_REVENUE_INUSD decimal(19,2),
        DESCRIPTION longtext,
        EMAIL varchar(255),
        MAIN_PHONE_COUNTRY_CODE integer,
        MAIN_PHONE_PHONE_NUMBER bigint,
        NAME varchar(64) not null,
        NUMBER_OF_EMPLOYEES integer check (NUMBER_OF_EMPLOYEES>=0),
        TICKER_SYMBOL varchar(25),
        WEBSITE varchar(255),
        ASSIGNED_TO_ID bigint,
        BILLING_ADDRESS_ID bigint not null,
        CURRENCY_ID varchar(255),
        INDUSTRY_ID varchar(255),
        MAILING_ADDRESS_ID bigint,
        primary key (ID)
    );

    create table sample.ACCOUNT_ACCOUNT_TYPE (
        ACCOUNT_ID bigint not null,
        ACCOUNT_TYPES_ID varchar(255) not null,
        primary key (ACCOUNT_ID, ACCOUNT_TYPES_ID)
    );

    create table sample.ACCOUNT_TYPE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table sample.ADDRESS (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ADDRESS_TYPE varchar(255) not null,
        CITY varchar(32) not null,
        STREET varchar(32) not null,
        ZIP_CODE varchar(255),
        COUNTRY_ID varchar(255) not null,
        STATE_ID varchar(255),
        primary key (ID)
    );

    create table sample.CONTACT (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        BIRTH_DATE date,
        DEPARTMENT varchar(64),
        DESCRIPTION longtext,
        DO_NOT_CALL bit not null,
        DO_NOT_EMAIL bit not null,
        EMAIL varchar(255),
        FIRST_NAME varchar(64) not null,
        LAST_NAME varchar(64) not null,
        MAIN_PHONE_COUNTRY_CODE integer,
        MAIN_PHONE_PHONE_NUMBER bigint,
        MAIN_PHONE_TYPE varchar(255) not null,
        OTHER_PHONE_COUNTRY_CODE integer,
        OTHER_PHONE_PHONE_NUMBER bigint,
        OTHER_PHONE_TYPE varchar(255) not null,
        TITLE varchar(64),
        ACCOUNT_ID bigint,
        ASSIGNED_TO_ID bigint,
        LEAD_SOURCE_ID varchar(255),
        MAILING_ADDRESS_ID bigint not null,
        OTHER_ADDRESS_ID bigint,
        REPORTS_TO_ID bigint,
        primary key (ID)
    );

    create table sample.COUNTRY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        COUNTRY_TYPE varchar(255),
        MAX_POSTAL_CODE varchar(255),
        MIN_POSTAL_CODE varchar(255),
        CURRENCY_ID varchar(255),
        primary key (ID)
    );

    create table sample.CURRENCY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table sample.INDUSTRY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table sample.LEAD_SOURCE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table sample.OPPORTUNITY (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ACTUAL_CLOSE_DATE date,
        AMOUNT decimal(19,2) check (AMOUNT>=0),
        AMOUNT_INUSD decimal(19,2),
        AMOUNT_WEIGHTED_INUSD decimal(19,2),
        DESCRIPTION longtext,
        EXPECTED_CLOSE_DATE date,
        NAME varchar(64) not null,
        OPPORTUNITY_TYPE varchar(255),
        PROBABILITY double precision not null,
        ACCOUNT_ID bigint,
        ASSIGNED_TO_ID bigint,
        CURRENCY_ID varchar(255),
        LEAD_SOURCE_ID varchar(255),
        SALES_STAGE_ID varchar(255),
        primary key (ID)
    );

    create table sample.PERMISSION (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        CREATE_ALLOWED bit not null,
        DELETE_ALLOWED bit not null,
        EDIT_ALLOWED bit not null,
        FIELD varchar(255),
        TARGET_TYPE varchar(64) not null,
        VIEW_ALLOWED bit not null,
        ROLE_ID bigint,
        primary key (ID),
        unique (TARGET_TYPE, FIELD)
    );

    create table sample.PROFILE (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        COMPANY_WEBSITE varchar(255) not null,
        EMAIL varchar(255) not null,
        FIRST_NAME varchar(64) not null,
        LAST_NAME varchar(64) not null,
        PHONE_COUNTRY_CODE integer,
        PHONE_PHONE_NUMBER bigint,
        PHONE_TYPE varchar(255),
        TITLE varchar(64),
        USER_ID bigint not null,
        primary key (ID)
    );

    create table sample.ROLE (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ALLOW_OR_DENY_BY_DEFAULT varchar(255),
        DESCRIPTION longtext,
        NAME varchar(64) not null,
        primary key (ID)
    );

    create table sample.SALES_STAGE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        PROBABILITY double precision not null,
        primary key (ID)
    );

    create table sample.STATE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        CODE varchar(255),
        STATE_TYPE varchar(255),
        COUNTRY_ID varchar(255) not null,
        primary key (ID)
    );

    create table sample.USER (
        ID bigint not null auto_increment,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ACCOUNT_EXPIRED bit not null,
        ACCOUNT_LOCKED bit not null,
        CREDENTIALS_EXPIRED bit not null,
        ENABLED bit not null,
        LOGIN_NAME varchar(16) not null,
        LOGIN_PASSWORD_ENCRYPTED varchar(255),
        primary key (ID),
        unique (LOGIN_NAME)
    );

    create table sample.USER_ROLE (
        ROLE_ID bigint not null,
        USER_ID bigint not null,
        CREATED datetime not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED datetime not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        primary key (ROLE_ID, USER_ID)
    );

    create index IDX_ACCOUNT_ASSIGNED_TO on sample.ACCOUNT (ASSIGNED_TO_ID);

    create index IDX_ACCOUNT_CURRENCY on sample.ACCOUNT (CURRENCY_ID);

    create index IDX_ACCOUNT_BILLING_ADDRESS on sample.ACCOUNT (BILLING_ADDRESS_ID);

    create index IDX_ACCOUNT_SHIPPING_ADDRESS on sample.ACCOUNT (MAILING_ADDRESS_ID);

    create index IDX_ACCOUNT_INDUSTRY on sample.ACCOUNT (INDUSTRY_ID);

    alter table sample.ACCOUNT 
        add index FK_ACCOUNT_CURRENCY (CURRENCY_ID), 
        add constraint FK_ACCOUNT_CURRENCY 
        foreign key (CURRENCY_ID) 
        references sample.CURRENCY (ID);

    alter table sample.ACCOUNT 
        add index FK_ACCOUNT_SHIPPING_ADDRESS (MAILING_ADDRESS_ID), 
        add constraint FK_ACCOUNT_SHIPPING_ADDRESS 
        foreign key (MAILING_ADDRESS_ID) 
        references sample.ADDRESS (ID);

    alter table sample.ACCOUNT 
        add index FK_ACCOUNT_INDUSTRY (INDUSTRY_ID), 
        add constraint FK_ACCOUNT_INDUSTRY 
        foreign key (INDUSTRY_ID) 
        references sample.INDUSTRY (ID);

    alter table sample.ACCOUNT 
        add index FK_ACCOUNT_ASSIGNED_TO (ASSIGNED_TO_ID), 
        add constraint FK_ACCOUNT_ASSIGNED_TO 
        foreign key (ASSIGNED_TO_ID) 
        references sample.USER (ID);

    alter table sample.ACCOUNT 
        add index FK_ACCOUNT_BILLING_ADDRESS (BILLING_ADDRESS_ID), 
        add constraint FK_ACCOUNT_BILLING_ADDRESS 
        foreign key (BILLING_ADDRESS_ID) 
        references sample.ADDRESS (ID);

    alter table sample.ACCOUNT_ACCOUNT_TYPE 
        add index FK_ACCOUNT_ACCOUNT (ACCOUNT_ID), 
        add constraint FK_ACCOUNT_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references sample.ACCOUNT (ID);

    alter table sample.ACCOUNT_ACCOUNT_TYPE 
        add index FK_ACCOUNT_ACCOUNT_TYPES (ACCOUNT_TYPES_ID), 
        add constraint FK_ACCOUNT_ACCOUNT_TYPES 
        foreign key (ACCOUNT_TYPES_ID) 
        references sample.ACCOUNT_TYPE (ID);

    create index IDX_ADDRESS_COUNTRY on sample.ADDRESS (COUNTRY_ID);

    create index IDX_ADDRESS_STATE on sample.ADDRESS (STATE_ID);

    alter table sample.ADDRESS 
        add index FK_ADDRESS_STATE (STATE_ID), 
        add constraint FK_ADDRESS_STATE 
        foreign key (STATE_ID) 
        references sample.STATE (ID);

    alter table sample.ADDRESS 
        add index FK_ADDRESS_COUNTRY (COUNTRY_ID), 
        add constraint FK_ADDRESS_COUNTRY 
        foreign key (COUNTRY_ID) 
        references sample.COUNTRY (ID);

    create index IDX_CONTACT_OTHER_ADDRESS on sample.CONTACT (OTHER_ADDRESS_ID);

    create index IDX_CONTACT_ASSIGNED_TO on sample.CONTACT (ASSIGNED_TO_ID);

    create index IDX_CONTACT_MAILING_ADDRESS on sample.CONTACT (MAILING_ADDRESS_ID);

    create index IDX_CONTACT_ACCOUNT on sample.CONTACT (ACCOUNT_ID);

    create index IDX_CONTACT_LEAD_SOURCE on sample.CONTACT (LEAD_SOURCE_ID);

    create index IDX_CONTACT_REPORTS_TO on sample.CONTACT (REPORTS_TO_ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_MAILING_ADDRESS (MAILING_ADDRESS_ID), 
        add constraint FK_CONTACT_MAILING_ADDRESS 
        foreign key (MAILING_ADDRESS_ID) 
        references sample.ADDRESS (ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_REPORTS_TO (REPORTS_TO_ID), 
        add constraint FK_CONTACT_REPORTS_TO 
        foreign key (REPORTS_TO_ID) 
        references sample.CONTACT (ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_ASSIGNED_TO (ASSIGNED_TO_ID), 
        add constraint FK_CONTACT_ASSIGNED_TO 
        foreign key (ASSIGNED_TO_ID) 
        references sample.USER (ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_ACCOUNT (ACCOUNT_ID), 
        add constraint FK_CONTACT_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references sample.ACCOUNT (ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_LEAD_SOURCE (LEAD_SOURCE_ID), 
        add constraint FK_CONTACT_LEAD_SOURCE 
        foreign key (LEAD_SOURCE_ID) 
        references sample.LEAD_SOURCE (ID);

    alter table sample.CONTACT 
        add index FK_CONTACT_OTHER_ADDRESS (OTHER_ADDRESS_ID), 
        add constraint FK_CONTACT_OTHER_ADDRESS 
        foreign key (OTHER_ADDRESS_ID) 
        references sample.ADDRESS (ID);

    create index IDX_COUNTRY_CURRENCY on sample.COUNTRY (CURRENCY_ID);

    alter table sample.COUNTRY 
        add index FK_COUNTRY_CURRENCY (CURRENCY_ID), 
        add constraint FK_COUNTRY_CURRENCY 
        foreign key (CURRENCY_ID) 
        references sample.CURRENCY (ID);

    create index IDX_OPPORTUNITY_LEAD_SOURCE on sample.OPPORTUNITY (LEAD_SOURCE_ID);

    create index IDX_OPPORTUNITY_USER on sample.OPPORTUNITY (ASSIGNED_TO_ID);

    create index IDX_OPPORTUNITY_ACCOUNT on sample.OPPORTUNITY (ACCOUNT_ID);

    create index IDX_OPPORTUNITY_SALES_STAGE on sample.OPPORTUNITY (SALES_STAGE_ID);

    create index IDX_OPPORTUNITY_CURRENCY on sample.OPPORTUNITY (CURRENCY_ID);

    alter table sample.OPPORTUNITY 
        add index FK_OPPORTUNITY_CURRENCY (CURRENCY_ID), 
        add constraint FK_OPPORTUNITY_CURRENCY 
        foreign key (CURRENCY_ID) 
        references sample.CURRENCY (ID);

    alter table sample.OPPORTUNITY 
        add index FK_OPPORTUNITY_SALES_STAGE (SALES_STAGE_ID), 
        add constraint FK_OPPORTUNITY_SALES_STAGE 
        foreign key (SALES_STAGE_ID) 
        references sample.SALES_STAGE (ID);

    alter table sample.OPPORTUNITY 
        add index FK_OPPORTUNITY_USER (ASSIGNED_TO_ID), 
        add constraint FK_OPPORTUNITY_USER 
        foreign key (ASSIGNED_TO_ID) 
        references sample.USER (ID);

    alter table sample.OPPORTUNITY 
        add index FK_OPPORTUNITY_ACCOUNT (ACCOUNT_ID), 
        add constraint FK_OPPORTUNITY_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references sample.ACCOUNT (ID);

    alter table sample.OPPORTUNITY 
        add index FK_OPPORTUNITY_LEAD_SOURCE (LEAD_SOURCE_ID), 
        add constraint FK_OPPORTUNITY_LEAD_SOURCE 
        foreign key (LEAD_SOURCE_ID) 
        references sample.LEAD_SOURCE (ID);

    create index IDX_PERMISSION_ROLE on sample.PERMISSION (ROLE_ID);

    alter table sample.PERMISSION 
        add index FK_PERMISSION_ROLE (ROLE_ID), 
        add constraint FK_PERMISSION_ROLE 
        foreign key (ROLE_ID) 
        references sample.ROLE (ID);

    create index IDX_PROFILE_USER on sample.PROFILE (USER_ID);

    alter table sample.PROFILE 
        add index FK_PROFILE_USER (USER_ID), 
        add constraint FK_PROFILE_USER 
        foreign key (USER_ID) 
        references sample.USER (ID);

    create index IDX_STATE_COUNTRY on sample.STATE (COUNTRY_ID);

    alter table sample.STATE 
        add index FK_STATE_COUNTRY (COUNTRY_ID), 
        add constraint FK_STATE_COUNTRY 
        foreign key (COUNTRY_ID) 
        references sample.COUNTRY (ID);

    create index IDX_USER_ROLE_ROLE on sample.USER_ROLE (ROLE_ID);

    create index IDX_USER_ROLE_USER on sample.USER_ROLE (USER_ID);

    alter table sample.USER_ROLE 
        add index FK_USER_ROLE_ROLE (ROLE_ID), 
        add constraint FK_USER_ROLE_ROLE 
        foreign key (ROLE_ID) 
        references sample.ROLE (ID);

    alter table sample.USER_ROLE 
        add index FK_USER_ROLE_USER (USER_ID), 
        add constraint FK_USER_ROLE_USER 
        foreign key (USER_ID) 
        references sample.USER (ID);
