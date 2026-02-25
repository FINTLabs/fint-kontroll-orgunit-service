

create table org_unit_all_sub_org_units_ref (
                                       org_unit_id int8 not null,
                                       all_sub_org_units_ref varchar(255));

alter table if exists org_unit_all_sub_org_units_ref add constraint fk_org_unit_all_sub_org_units_ref_org_unit
    foreign key (org_unit_id) references org_unit;
