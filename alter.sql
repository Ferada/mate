alter table mate_channelpriorities add column id int not null unique;
alter table mate_channelpriorities add unique (id);
alter table mate_channelpriorities drop constraint mate_channelpriorities_id_key;
alter table mate_channelpriorities add primary key (id);

alter table mate_deviceaccesses add column id int not null unique;
alter table mate_deviceaccesses drop constraint mate_deviceaccesses_id_key;
alter table mate_deviceaccesses add primary key (id);

alter table mate_privacy add column id int;
create sequence mate_privacy_id_seq;
update mate_privacy set id = nextval (mate_privacy_id_seq);
drop sequence mate_privacy_id_seq;
alter table mate_privacy alter column id set not null;
alter table mate_privacy add unique (id);
alter table mate_privacy drop constraint mate_privacy_id_key;
alter table mate_privacy add primary key (id);

alter table mate_roomdevices add column id int not null unique;
alter table mate_roomdevices drop constraint mate_roomdevices_id_key;
alter table mate_roomdevices add primary key (id);

alter table mate_userdevices add column id int not null unique;
alter table mate_userdevices drop constraint mate_userdevices_id_key;
alter table mate_userdevices add primary key (id);

