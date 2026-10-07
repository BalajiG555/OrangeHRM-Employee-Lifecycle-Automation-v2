# Test data

* `employee.json` - template for generated employees. Names get a random suffix and the Employee Id
  gets a per-environment prefix (`testdata.employee.id.prefix`) plus random digits, so data is unique
  even when scenarios run in parallel.
* Environment-specific overrides: create `testdata/<env>/employee.json` (e.g. `testdata/qa/employee.json`)
  when an environment uses different reference data (job titles, employment statuses). It is picked up
  automatically with `-Denv=<env>`; no code changes needed.
* `profile.png` - picture uploaded when an employee is created through the UI.
