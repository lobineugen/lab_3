drop table lw_params;
drop table lw_visit;
drop table lw_aot;
drop table lw_attr;
drop table lw_objects;
drop table lw_object_types;
drop table lw_right;


create table lw_object_types (
object_type_id number(20) not null,
parent_id number(20),
name varchar2(20),
constraint object_type_pk primary key (object_type_id),
constraint object_type_p foreign key (parent_id) references lw_object_types (object_type_id)
);

create table lw_objects (
object_id number(20) not null,
parent_id number(20),
object_type_id number(20) not null,
name varchar2(20),
constraint objects_pk primary key (object_id),
constraint objects_fk foreign key (object_type_id) references lw_object_types(object_type_id),
constraint objects_p1k foreign key (parent_id) references lw_objects(object_id) on delete cascade
);

create table lw_attr(
attr_id number(20) not null,
name varchar2(20),
constraint attr_pk primary key (attr_id)
);

create table lw_aot(
attr_id number(20) not null,
object_type_id number not null,
constraint objects_fk5 foreign key (attr_id) references lw_attr(attr_id),
constraint objects_fk6 foreign key (object_type_id) references lw_object_types(object_type_id)
);

create table lw_params(
object_id number(20) not null,
attr_id number(20) not null,
value varchar2(20),
constraint params_attr_fk foreign key (attr_id) references lw_attr(attr_id),
constraint params_objects_fk foreign key (object_id) references lw_objects(object_id) on delete cascade
);

create table lw_visit (
object_id number,
lesson_id number,
editDate varchar2(20),
mark varchar2(10),
CONSTRAINT visit_object_fk FOREIGN KEY (object_id) REFERENCES lw_objects(object_id),
CONSTRAINT visit_lesson_fk FOREIGN KEY (lesson_id) REFERENCES lw_objects(object_id)
);


create table lw_right (
user_id number,
name VARCHAR2(10),
right VARCHAR2(20)
);

CREATE SEQUENCE sss
  START WITH 100
  INCREMENT BY 1;

insert into lw_right values (1,'admin','FULL');
insert into lw_right values (2,'teacher','EDIT');
insert into lw_right values (3,'user','INFO');

insert into lw_object_types values (1,null,'University');
insert into lw_object_types values (2,1,'Group');
insert into lw_object_types values (3,1,'Department');
insert into lw_object_types values (4,2,'Student');
insert into lw_object_types values (5,3,'Teacher');
insert into lw_object_types values (6,1,'Lessons');


insert into lw_attr values (1,'Address');
insert into lw_attr values (2,'Kurator');
insert into lw_attr values (3,'Dekan');
insert into lw_attr values (4,'Last_Name');
insert into lw_attr values (5,'Age');
insert into lw_attr values (6,'Scholarship');
insert into lw_attr values (7,'Salary');
insert into lw_attr values (8,'Classroom');
insert into lw_attr values (9,'Lessons');


insert into lw_aot values (1,1);
insert into lw_aot values (2,2);
insert into lw_aot values (3,3);
insert into lw_aot values (4,4);
insert into lw_aot values (4,5);
insert into lw_aot values (5,4);
insert into lw_aot values (5,5);
insert into lw_aot values (6,4);
insert into lw_aot values (7,5);
insert into lw_aot values (8,6);
insert into lw_aot values (9,4);


insert into lw_objects values (1,null,1,'SumDU');
insert into lw_objects values (2,1,2,'IN-53');
insert into lw_objects values (3,1,2,'IT-01');
insert into lw_objects values (4,1,3,'ELIT');
insert into lw_objects values (5,1,3,'EKONOM');
insert into lw_objects values (6,2,4,'Sergey');	
insert into lw_objects values (7,2,4,'Andrey'); 
insert into lw_objects values (8,2,4,'Petr'); 
insert into lw_objects values (9,3,4,'Vanya'); 
insert into lw_objects values (10,3,4,'Sasha'); 
insert into lw_objects values (11,4,5,'Sergey');
insert into lw_objects values (12,4,5,'Nikolaj'); 
insert into lw_objects values (13,4,5,'Andrey'); 
insert into lw_objects values (14,5,5,'Petr');
insert into lw_objects values (15,5,5,'Evgenij');
insert into lw_objects values (16,1,6,'Mathematics');
insert into lw_objects values (17,1,6,'Physics');
insert into lw_objects values (18,1,6,'Logic');
insert into lw_objects values (19,1,6,'English');
insert into lw_objects values (20,1,6,'Philosophy');


insert into lw_params values (1,1,'Supruna 11');
insert into lw_params values (2,2,'13');
insert into lw_params values (3,2,'14');
insert into lw_params values (4,3,'12');
insert into lw_params values (5,3,'11');
insert into lw_params values (6,4,'Petrov');
insert into lw_params values (6,5,'20');
insert into lw_params values (6,6,'1000');
insert into lw_params values (7,4,'Sidorov');
insert into lw_params values (7,5,'20');
insert into lw_params values (7,6,'1100');
insert into lw_params values (7,9,'16');
insert into lw_params values (7,9,'17');
insert into lw_params values (8,4,'Ivanov');
insert into lw_params values (8,5,'18');
insert into lw_params values (8,6,'1200');
insert into lw_params values (8,9,'18');
insert into lw_params values (8,9,'19');

insert into lw_params values (9,4,'Korobov');
insert into lw_params values (9,5,'19');
insert into lw_params values (9,6,'1200');
insert into lw_params values (9,9,'16');
insert into lw_params values (9,9,'20');
insert into lw_params values (10,4,'Somov');
insert into lw_params values (10,5,'20');
insert into lw_params values (10,6,'1100');
insert into lw_params values (10,9,'17');

insert into lw_params values (10,9,'19');
insert into lw_params values (11,4,'Razin');
insert into lw_params values (11,5,'40');
insert into lw_params values (11,7,'5000');

insert into lw_params values (12,4,'Gromov');
insert into lw_params values (12,5,'45');
insert into lw_params values (12,7,'7000');

insert into lw_params values (13,4,'Vasiliev');
insert into lw_params values (13,5,'50');
insert into lw_params values (13,7,'6000');

insert into lw_params values (14,4,'Nosov');
insert into lw_params values (14,5,'52');
insert into lw_params values (14,7,'6000');

insert into lw_params values (15,4,'Krasnov');
insert into lw_params values (15,5,'55');
insert into lw_params values (15,7,'8000');

insert into lw_params values (16,8,'ET-305');
insert into lw_params values (17,8,'C-210');
insert into lw_params values (18,8,'C-312');
insert into lw_params values (19,8,'M-110');
insert into lw_params values (20,8,'ET-115');