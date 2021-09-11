--
-- CognitiveScale Cortex
--
-- Copyright (c) Cognitive Scale Inc.
-- All rights reserved.
-- Dissemination or any rights to code or any derivative works thereof is strictly forbidden
-- unless licensed and subject to a separate written agreement with CognitiveScale.
--

create table tester (
  id int(8) NOT NULL,
  desc varchar(255) DEFAULT NULL,
);

alter table tester add primary key (id);