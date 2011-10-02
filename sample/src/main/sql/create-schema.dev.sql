
    create table SAMPLE.ACCOUNT (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ANNUAL_REVENUE decimal(19,2) check (ANNUAL_REVENUE>=0),
        ANNUAL_REVENUE_INUSD decimal(19,2),
        DESCRIPTION clob,
        EMAIL varchar(255),
        MAIN_PHONE_COUNTRY_CODE integer,
        MAIN_PHONE_PHONE_NUMBER bigint,
        MAIN_PHONE_PHONE_TYPE varchar(255),
        NAME varchar(64) not null,
        NUMBER_OF_EMPLOYEES integer check (NUMBER_OF_EMPLOYEES>=0),
        TICKER_SYMBOL varchar(25),
        WEBSITE varchar(64),
        ASSIGNED_TO_ID bigint,
        BILLING_ADDRESS_ID bigint not null,
        CURRENCY_ID varchar(255),
        INDUSTRY_ID varchar(255),
        MAILING_ADDRESS_ID bigint,
        primary key (ID)
    );

    create table SAMPLE.ACCOUNT_ACCOUNT_TYPE (
        ACCOUNT_ID bigint not null,
        ACCOUNT_TYPES_ID varchar(255) not null,
        primary key (ACCOUNT_ID, ACCOUNT_TYPES_ID)
    );

    create table SAMPLE.ACCOUNT_TYPE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table SAMPLE.ADDRESS (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ADDRESS_TYPE varchar(255) not null,
        CITY varchar(16) not null,
        STREET varchar(16) not null,
        ZIP_CODE varchar(255),
        COUNTRY_ID varchar(255) not null,
        STATE_ID varchar(255),
        primary key (ID)
    );

    create table SAMPLE.CONTACT (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        BIRTH_DATE date,
        DEPARTMENT varchar(64),
        DESCRIPTION clob,
        DO_NOT_CALL boolean not null,
        DO_NOT_EMAIL boolean not null,
        EMAIL varchar(255),
        FIRST_NAME varchar(64) not null,
        LAST_NAME varchar(64) not null,
        MAIN_PHONE_COUNTRY_CODE integer,
        MAIN_PHONE_PHONE_NUMBER bigint,
        MAIN_PHONE_PHONE_TYPE varchar(255),
        OTHER_PHONE_COUNTRY_CODE integer,
        OTHER_PHONE_PHONE_NUMBER bigint,
        OTHER_PHONE_PHONE_TYPE varchar(255),
        TITLE varchar(64),
        ACCOUNT_ID bigint,
        ASSIGNED_TO_ID bigint,
        LEAD_SOURCE_ID varchar(255),
        MAILING_ADDRESS_ID bigint not null,
        OTHER_ADDRESS_ID bigint,
        REPORTS_TO_ID bigint,
        primary key (ID)
    );

    create table SAMPLE.COUNTRY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        COUNTRY_TYPE varchar(255),
        MAX_POSTAL_CODE varchar(255),
        MIN_POSTAL_CODE varchar(255),
        CURRENCY_ID varchar(255),
        primary key (ID)
    );

    create table SAMPLE.CURRENCY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table SAMPLE.INDUSTRY (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table SAMPLE.LEAD_SOURCE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        primary key (ID)
    );

    create table SAMPLE.OPPORTUNITY (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        AMOUNT decimal(19,2) check (AMOUNT>=0),
        AMOUNT_WEIGHTED_INUSD decimal(19,2),
        DESCRIPTION clob,
        EXPECTED_CLOSE_DATE date,
        NAME varchar(64) not null,
        OPPORTUNITY_TYPE varchar(255),
        PROBABILITY double not null,
        ACCOUNT_ID bigint,
        ASSIGNED_TO_ID bigint,
        CURRENCY_ID varchar(255),
        LEAD_SOURCE_ID varchar(255),
        SALES_STAGE_ID varchar(255),
        primary key (ID)
    );

    create table SAMPLE.PERMISSION (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        CREATE boolean not null,
        DELETE boolean not null,
        EDIT boolean not null,
        ENTITY_TYPE varchar(64) not null,
        FIELD varchar(255),
        VIEW boolean not null,
        ROLE_ID bigint,
        primary key (ID),
        unique (ENTITY_TYPE, FIELD)
    );

    create table SAMPLE.ROLE (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ALLOW_OR_DENY_BY_DEFAULT varchar(255),
        DESCRIPTION clob,
        NAME varchar(64) not null,
        primary key (ID)
    );

    create table SAMPLE.SALES_STAGE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        PROBABILITY double not null,
        primary key (ID)
    );

    create table SAMPLE.STATE (
        ID varchar(255) not null,
        DISPLAY_NAME varchar(255),
        SORT_ORDER integer,
        CODE varchar(255),
        STATE_TYPE varchar(255),
        COUNTRY_ID varchar(255) not null,
        primary key (ID)
    );

    create table SAMPLE.USER (
        ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        UUID varchar(255) not null unique,
        ACCOUNT_EXPIRED boolean not null,
        ACCOUNT_LOCKED boolean not null,
        CREDENTIALS_EXPIRED boolean not null,
        ENABLED boolean not null,
        LOGIN_NAME varchar(16) not null,
        LOGIN_PASSWORD varchar(16) not null,
        primary key (ID),
        unique (LOGIN_NAME)
    );

    create table SAMPLE.USER_ROLE (
        ROLE_ID bigint not null,
        USER_ID bigint not null,
        CREATED timestamp not null,
        CREATED_BY varchar(255) not null,
        LAST_MODIFIED timestamp not null,
        MODIFIED_BY varchar(255) not null,
        VERSION integer,
        primary key (ROLE_ID, USER_ID)
    );

    create index IDX_ACCOUNT_ASSIGNED_TO on SAMPLE.ACCOUNT (ASSIGNED_TO_ID);

    create index IDX_ACCOUNT_CURRENCY on SAMPLE.ACCOUNT (CURRENCY_ID);

    create index IDX_ACCOUNT_BILLING_ADDRESS on SAMPLE.ACCOUNT (BILLING_ADDRESS_ID);

    create index IDX_ACCOUNT_SHIPPING_ADDRESS on SAMPLE.ACCOUNT (MAILING_ADDRESS_ID);

    create index IDX_ACCOUNT_INDUSTRY on SAMPLE.ACCOUNT (INDUSTRY_ID);

    alter table SAMPLE.ACCOUNT 
        add constraint FK_ACCOUNT_CURRENCY 
        foreign key (CURRENCY_ID) 
        references SAMPLE.CURRENCY;

    alter table SAMPLE.ACCOUNT 
        add constraint FK_ACCOUNT_SHIPPING_ADDRESS 
        foreign key (MAILING_ADDRESS_ID) 
        references SAMPLE.ADDRESS;

    alter table SAMPLE.ACCOUNT 
        add constraint FK_ACCOUNT_INDUSTRY 
        foreign key (INDUSTRY_ID) 
        references SAMPLE.INDUSTRY;

    alter table SAMPLE.ACCOUNT 
        add constraint FK_ACCOUNT_ASSIGNED_TO 
        foreign key (ASSIGNED_TO_ID) 
        references SAMPLE.USER;

    alter table SAMPLE.ACCOUNT 
        add constraint FK_ACCOUNT_BILLING_ADDRESS 
        foreign key (BILLING_ADDRESS_ID) 
        references SAMPLE.ADDRESS;

    alter table SAMPLE.ACCOUNT_ACCOUNT_TYPE 
        add constraint FK_ACCOUNT_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references SAMPLE.ACCOUNT;

    alter table SAMPLE.ACCOUNT_ACCOUNT_TYPE 
        add constraint FK_ACCOUNT_ACCOUNT_TYPES 
        foreign key (ACCOUNT_TYPES_ID) 
        references SAMPLE.ACCOUNT_TYPE;

    create index IDX_ADDRESS_COUNTRY on SAMPLE.ADDRESS (COUNTRY_ID);

    create index IDX_ADDRESS_STATE on SAMPLE.ADDRESS (STATE_ID);

    alter table SAMPLE.ADDRESS 
        add constraint FK_ADDRESS_STATE 
        foreign key (STATE_ID) 
        references SAMPLE.STATE;

    alter table SAMPLE.ADDRESS 
        add constraint FK_ADDRESS_COUNTRY 
        foreign key (COUNTRY_ID) 
        references SAMPLE.COUNTRY;

    create index IDX_CONTACT_OTHER_ADDRESS on SAMPLE.CONTACT (OTHER_ADDRESS_ID);

    create index IDX_CONTACT_ASSIGNED_TO on SAMPLE.CONTACT (ASSIGNED_TO_ID);

    create index IDX_CONTACT_MAILING_ADDRESS on SAMPLE.CONTACT (MAILING_ADDRESS_ID);

    create index IDX_CONTACT_ACCOUNT on SAMPLE.CONTACT (ACCOUNT_ID);

    create index IDX_CONTACT_LEAD_SOURCE on SAMPLE.CONTACT (LEAD_SOURCE_ID);

    create index IDX_CONTACT_REPORTS_TO on SAMPLE.CONTACT (REPORTS_TO_ID);

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_MAILING_ADDRESS 
        foreign key (MAILING_ADDRESS_ID) 
        references SAMPLE.ADDRESS;

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_REPORTS_TO 
        foreign key (REPORTS_TO_ID) 
        references SAMPLE.CONTACT;

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_ASSIGNED_TO 
        foreign key (ASSIGNED_TO_ID) 
        references SAMPLE.USER;

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references SAMPLE.ACCOUNT;

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_LEAD_SOURCE 
        foreign key (LEAD_SOURCE_ID) 
        references SAMPLE.LEAD_SOURCE;

    alter table SAMPLE.CONTACT 
        add constraint FK_CONTACT_OTHER_ADDRESS 
        foreign key (OTHER_ADDRESS_ID) 
        references SAMPLE.ADDRESS;

    create index IDX_COUNTRY_CURRENCY on SAMPLE.COUNTRY (CURRENCY_ID);

    alter table SAMPLE.COUNTRY 
        add constraint FK_COUNTRY_CURRENCY 
        foreign key (CURRENCY_ID) 
        references SAMPLE.CURRENCY;

    create index IDX_OPPORTUNITY_LEAD_SOURCE on SAMPLE.OPPORTUNITY (LEAD_SOURCE_ID);

    create index IDX_OPPORTUNITY_USER on SAMPLE.OPPORTUNITY (ASSIGNED_TO_ID);

    create index IDX_OPPORTUNITY_ACCOUNT on SAMPLE.OPPORTUNITY (ACCOUNT_ID);

    create index IDX_OPPORTUNITY_SALES_STAGE on SAMPLE.OPPORTUNITY (SALES_STAGE_ID);

    create index IDX_OPPORTUNITY_CURRENCY on SAMPLE.OPPORTUNITY (CURRENCY_ID);

    alter table SAMPLE.OPPORTUNITY 
        add constraint FK_OPPORTUNITY_CURRENCY 
        foreign key (CURRENCY_ID) 
        references SAMPLE.CURRENCY;

    alter table SAMPLE.OPPORTUNITY 
        add constraint FK_OPPORTUNITY_SALES_STAGE 
        foreign key (SALES_STAGE_ID) 
        references SAMPLE.SALES_STAGE;

    alter table SAMPLE.OPPORTUNITY 
        add constraint FK_OPPORTUNITY_USER 
        foreign key (ASSIGNED_TO_ID) 
        references SAMPLE.USER;

    alter table SAMPLE.OPPORTUNITY 
        add constraint FK_OPPORTUNITY_ACCOUNT 
        foreign key (ACCOUNT_ID) 
        references SAMPLE.ACCOUNT;

    alter table SAMPLE.OPPORTUNITY 
        add constraint FK_OPPORTUNITY_LEAD_SOURCE 
        foreign key (LEAD_SOURCE_ID) 
        references SAMPLE.LEAD_SOURCE;

    create index PERMISSIONIDX_PERMISSION_ROLE on SAMPLE.PERMISSION (ROLE_ID);

    create index IDX_STATE_COUNTRY on SAMPLE.STATE (COUNTRY_ID);

    alter table SAMPLE.STATE 
        add constraint FK_STATE_COUNTRY 
        foreign key (COUNTRY_ID) 
        references SAMPLE.COUNTRY;

    create index USER_ROLEIDX_USER_ROLE_ROLE on SAMPLE.USER_ROLE (ROLE_ID);

    create index USER_ROLEIDX_USER_ROLE_USER on SAMPLE.USER_ROLE (USER_ID);

    create sequence SAMPLE.SEQ_ABSTRACT_PERMISSION;

    create sequence SAMPLE.SEQ_ABSTRACT_ROLE;

    create sequence SAMPLE.SEQ_ABSTRACT_USER;

    create sequence SAMPLE.SEQ_ACCOUNT;

    create sequence SAMPLE.SEQ_ADDRESS;

    create sequence SAMPLE.SEQ_CONTACT;

    create sequence SAMPLE.SEQ_OPPORTUNITY;
