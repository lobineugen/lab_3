DECLARE
  count_table number;
  count_sequence number;

  type string_array is varray(7) of varchar2(20);
  table_list string_array := string_array('LW_PARAMS', 'LW_VISIT', 'LW_AOT', 'LW_ATTR', 'LW_OBJECTS',
                                          'LW_OBJECT_TYPES', 'LW_RIGHT');
  table_exist number;
begin

  select count(table_name) into count_table FROM user_tables WHERE table_name like 'LW_%';
  --select count(table_name) FROM user_tables WHERE table_name like 'LW_%';
  select count(sequence_name) into count_sequence from all_sequences where sequence_name = 'SEQUENCE_NEXT_ID';

  if count_table < 7 then
    for i in table_list.first .. table_list.last loop
      select
        case when
          exists(select 1 from user_tables where table_name like table_list(i))
          then
            1
        else
          0
        end  into table_exist
      from dual;

      if table_exist = 1 then
        execute immediate 'drop table ' ||  table_list(i);
      end if;
    end loop;

    if count_sequence = 1 then
      execute immediate 'drop sequence SEQUENCE_NEXT_ID';
    else
      execute immediate 'CREATE SEQUENCE SEQUENCE_NEXT_ID
        START WITH 100
        INCREMENT BY 1';
    end if;

    execute immediate 'create table lw_object_types (
      object_type_id number(20) not null,
      parent_id number(20),
    name varchar2(20),
    constraint object_type_pk primary key (object_type_id),
    constraint object_type_p foreign key (parent_id) references lw_object_types (object_type_id) on delete cascade
      )';

    execute immediate 'create table lw_objects (
      object_id number(20) not null,
      parent_id number(20),
      object_type_id number(20) not null,
    name varchar2(20),
    constraint objects_pk primary key (object_id),
    constraint objects_fk foreign key (object_type_id) references lw_object_types(object_type_id),
    constraint objects_p1k foreign key (parent_id) references lw_objects(object_id) on delete cascade
      )';

    execute immediate 'create table lw_attr(
      attr_id number(20) not null,
    name varchar2(20),
    constraint attr_pk primary key (attr_id)
      )';

    execute immediate 'create table lw_aot(
      attr_id number(20) not null,
      object_type_id number not null,
    constraint objects_fk5 foreign key (attr_id) references lw_attr(attr_id),
    constraint objects_fk6 foreign key (object_type_id) references lw_object_types(object_type_id)
      )';

    execute immediate 'create table lw_params(
      object_id number(20) not null,
      attr_id number(20) not null,
    value varchar2(20),
    constraint params_attr_fk foreign key (attr_id) references lw_attr(attr_id),
    constraint params_objects_fk foreign key (object_id) references lw_objects(object_id) on delete cascade
      )';

    execute immediate 'create table lw_visit (
      object_id number,
      lesson_id number,
      editDate varchar2(20),
      mark varchar2(10),
    CONSTRAINT visit_object_fk FOREIGN KEY (object_id) REFERENCES lw_objects(object_id) delete cascade,
    CONSTRAINT visit_lesson_fk FOREIGN KEY (lesson_id) REFERENCES lw_objects(object_id)
      )';


    execute immediate 'create table lw_right (
      user_id number,
    name VARCHAR2(25),
    right VARCHAR2(20)
      )';



    execute immediate 'insert into lw_right values (1,''admin'',''FULL'')';
    execute immediate 'insert into lw_right values (2,''teacher'',''EDIT'')';
    execute immediate 'insert into lw_right values (3,''user'',''INFO'')';

    execute immediate 'insert into lw_object_types values (1,null,''University'')';
    execute immediate 'insert into lw_object_types values (2,1,''Group'')';
    execute immediate 'insert into lw_object_types values (3,1,''Department'')';
    execute immediate 'insert into lw_object_types values (4,2,''Student'')';
    execute immediate 'insert into lw_object_types values (5,3,''Teacher'')';
    execute immediate 'insert into lw_object_types values (6,1,''Lessons'')';


    execute immediate 'insert into lw_attr values (1,''Address'')';
    execute immediate 'insert into lw_attr values (2,''Kurator'')';
    execute immediate 'insert into lw_attr values (3,''Dekan'')';
    execute immediate 'insert into lw_attr values (4,''Last_Name'')';
    execute immediate 'insert into lw_attr values (5,''Age'')';
    execute immediate 'insert into lw_attr values (6,''Scholarship'')';
    execute immediate 'insert into lw_attr values (7,''Salary'')';
    execute immediate 'insert into lw_attr values (8,''Classroom'')';
    execute immediate 'insert into lw_attr values (9,''Lessons'')';


    execute immediate 'insert into lw_aot values (1,1)';
    execute immediate 'insert into lw_aot values (2,2)';
    execute immediate 'insert into lw_aot values (3,3)';
    execute immediate 'insert into lw_aot values (4,4)';
    execute immediate 'insert into lw_aot values (4,5)';
    execute immediate 'insert into lw_aot values (5,4)';
    execute immediate 'insert into lw_aot values (5,5)';
    execute immediate 'insert into lw_aot values (6,4)';
    execute immediate 'insert into lw_aot values (7,5)';
    execute immediate 'insert into lw_aot values (8,6)';
    execute immediate 'insert into lw_aot values (9,4)';


    execute immediate 'insert into lw_objects values (1,null,1,''SumDU'')';
    execute immediate 'insert into lw_objects values (2,1,2,''IN-53'')';
    execute immediate 'insert into lw_objects values (3,1,2,''IT-01'')';
    execute immediate 'insert into lw_objects values (4,1,3,''ELIT'')';
    execute immediate 'insert into lw_objects values (5,1,3,''EKONOM'')';
    execute immediate 'insert into lw_objects values (6,2,4,''Sergey'')';
    execute immediate 'insert into lw_objects values (7,2,4,''Andrey'')';
    execute immediate 'insert into lw_objects values (8,2,4,''Petr'')';
    execute immediate 'insert into lw_objects values (9,3,4,''Vanya'')';
    execute immediate 'insert into lw_objects values (10,3,4,''Sasha'')';
    execute immediate 'insert into lw_objects values (11,4,5,''Sergey'')';
    execute immediate 'insert into lw_objects values (12,4,5,''Nikolaj'')';
    execute immediate 'insert into lw_objects values (13,4,5,''Andrey'')';
    execute immediate 'insert into lw_objects values (14,5,5,''Petr'')';
    execute immediate 'insert into lw_objects values (15,5,5,''Evgenij'')';
    execute immediate 'insert into lw_objects values (16,1,6,''Mathematics'')';
    execute immediate 'insert into lw_objects values (17,1,6,''Physics'')';
    execute immediate 'insert into lw_objects values (18,1,6,''Logic'')';
    execute immediate 'insert into lw_objects values (19,1,6,''English'')';
    execute immediate 'insert into lw_objects values (20,1,6,''Philosophy'')';


    execute immediate 'insert into lw_params values (1,1,''Supruna 11'')';
    execute immediate 'insert into lw_params values (2,2,''13'')';
    execute immediate 'insert into lw_params values (3,2,''14'')';
    execute immediate 'insert into lw_params values (4,3,''12'')';
    execute immediate 'insert into lw_params values (5,3,''11'')';
    execute immediate 'insert into lw_params values (6,4,''Petrov'')';
    execute immediate 'insert into lw_params values (6,5,''20'')';
    execute immediate 'insert into lw_params values (6,6,''1000'')';
    execute immediate 'insert into lw_params values (7,4,''Sidorov'')';
    execute immediate 'insert into lw_params values (7,5,''20'')';
    execute immediate 'insert into lw_params values (7,6,''1100'')';
    execute immediate 'insert into lw_params values (7,9,''16'')';
    execute immediate 'insert into lw_params values (7,9,''17'')';
    execute immediate 'insert into lw_params values (8,4,''Ivanov'')';
    execute immediate 'insert into lw_params values (8,5,''18'')';
    execute immediate 'insert into lw_params values (8,6,''1200'')';
    execute immediate 'insert into lw_params values (8,9,''18'')';
    execute immediate 'insert into lw_params values (8,9,''19'')';

    execute immediate 'insert into lw_params values (9,4,''Korobov'')';
    execute immediate 'insert into lw_params values (9,5,''19'')';
    execute immediate 'insert into lw_params values (9,6,''1200'')';
    execute immediate 'insert into lw_params values (9,9,''16'')';
    execute immediate 'insert into lw_params values (9,9,''20'')';
    execute immediate 'insert into lw_params values (10,4,''Somov'')';
    execute immediate 'insert into lw_params values (10,5,''20'')';
    execute immediate 'insert into lw_params values (10,6,''1100'')';
    execute immediate 'insert into lw_params values (10,9,''17'')';

    execute immediate 'insert into lw_params values (10,9,''19'')';
    execute immediate 'insert into lw_params values (11,4,''Razin'')';
    execute immediate 'insert into lw_params values (11,5,''40'')';
    execute immediate 'insert into lw_params values (11,7,''5000'')';

    execute immediate 'insert into lw_params values (12,4,''Gromov'')';
    execute immediate 'insert into lw_params values (12,5,''45'')';
    execute immediate 'insert into lw_params values (12,7,''7000'')';

    execute immediate 'insert into lw_params values (13,4,''Vasiliev'')';
    execute immediate 'insert into lw_params values (13,5,''50'')';
    execute immediate 'insert into lw_params values (13,7,''6000'')';

    execute immediate 'insert into lw_params values (14,4,''Nosov'')';
    execute immediate 'insert into lw_params values (14,5,''52'')';
    execute immediate 'insert into lw_params values (14,7,''6000'')';

    execute immediate 'insert into lw_params values (15,4,''Krasnov'')';
    execute immediate 'insert into lw_params values (15,5,''55'')';
    execute immediate 'insert into lw_params values (15,7,''8000'')';

    execute immediate 'insert into lw_params values (16,8,''ET-305'')';
    execute immediate 'insert into lw_params values (17,8,''C-210'')';
    execute immediate 'insert into lw_params values (18,8,''C-312'')';
    execute immediate 'insert into lw_params values (19,8,''M-110'')';
    execute immediate 'insert into lw_params values (20,8,''ET-115'')';
    commit;
  end if;
end;

