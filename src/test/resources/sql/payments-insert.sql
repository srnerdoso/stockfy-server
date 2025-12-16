INSERT INTO public.payments
    (id, created_at, created_by, updated_at, updated_by, payment_status, total, customer_id, payment_method)
VALUES
    (100, '2025-12-16 18:44:14.526', 'marcos.duarte91@example.com', '2025-12-16 18:44:14.526', 'marcos.duarte91@example.com', 'PAID', 190.00, 2, 'DIGITAL'),
    (200, '2024-12-16 18:44:14.526', 'marcos.duarte91@example.com', '2024-12-16 18:44:14.526', 'marcos.duarte91@example.com', 'PAID', 150.00, 2, 'DIGITAL'),
    (300, '2025-12-16 18:44:14.526', 'marcos.duarte91@example.com', '2025-12-16 18:44:14.526', 'marcos.duarte91@example.com', 'REFUNDED', 190.00, 2, 'DIGITAL');