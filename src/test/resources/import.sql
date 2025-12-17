-- FIXME: Trocar isto quando descobrir uma forma melhor de resolver nomes das constraints no ambiente de testes

-- WARNING: Nome da constraint deve seguir o padrão "uk_<nome da entidade JPA>_<nome da coluna>"
-- Product
ALTER INDEX products_bar_code_key RENAME TO uk_product_bar_code;

-- Employee
ALTER INDEX employees_cpf_key RENAME TO uk_employee_cpf;
ALTER INDEX employee_contacts_email_key RENAME TO uk_employee_email;
ALTER INDEX employee_contacts_phone_number_key RENAME TO uk_employee_phone_number;

-- Customer
ALTER INDEX customers_cpf_key RENAME TO uk_customer_cpf;
ALTER INDEX customer_contacts_email_key RENAME TO uk_customer_email;
ALTER INDEX customer_contacts_phone_number_key RENAME TO uk_customer_phone_number;
