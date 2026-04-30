USE taskdb;

ALTER TABLE tasks DROP CHECK chk_status;
UPDATE tasks SET status = 'PENDING' WHERE status = 'CREATED';
ALTER TABLE tasks
  ADD CONSTRAINT chk_status
  CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'));

ALTER TABLE sub_tasks DROP CHECK chk_sub_status;
UPDATE sub_tasks SET status = 'PENDING' WHERE status = 'IN_PROGRESS';
ALTER TABLE sub_tasks
  ADD CONSTRAINT chk_sub_status
  CHECK (status IN ('PENDING', 'DONE'));
