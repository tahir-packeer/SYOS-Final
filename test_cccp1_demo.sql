-- CCCP1 Requirement 2b Test Data
-- This script adds test stock batches to demonstrate the expiry-date prioritized shelving

-- Reset stock batches and shelf stock for testing
DELETE FROM stock_batch WHERE item_id = (SELECT item_id
FROM item
WHERE code = 'EGG001');
UPDATE shelf_stock SET quantity = 0 WHERE item_id = (SELECT item_id
FROM item
WHERE code = 'EGG001');

-- Add test batches for EGG001 to demonstrate CCCP1 requirement 2b
-- Scenario: Older batch with later expiry vs Newer batch with sooner expiry

-- Batch 1: Older batch (received first) but expires later
INSERT INTO stock_batch
    (item_id, quantity_received, quantity_remaining, purchase_date, expiry_date)
SELECT item_id, 30, 30, '2025-09-20', '2025-11-15'
FROM item
WHERE code = 'EGG001';

-- Batch 2: Newer batch (received later) but expires sooner - this should be selected first!
INSERT INTO stock_batch
    (item_id, quantity_received, quantity_remaining, purchase_date, expiry_date)
SELECT item_id, 25, 25, '2025-09-25', '2025-10-10'
FROM item
WHERE code = 'EGG001';

-- Batch 3: Even newer batch with medium expiry
INSERT INTO stock_batch
    (item_id, quantity_received, quantity_remaining, purchase_date, expiry_date)
SELECT item_id, 20, 20, '2025-09-30', '2025-10-20'
FROM item
WHERE code = 'EGG001';

-- Check the test data
SELECT
    sb.batch_id,
    i.code,
    i.name,
    sb.quantity_received,
    sb.quantity_remaining,
    sb.purchase_date,
    sb.expiry_date,
    DATEDIFF(sb.expiry_date, CURDATE()) as days_to_expiry
FROM stock_batch sb
    JOIN item i ON sb.item_id = i.item_id
WHERE i.code = 'EGG001'
ORDER BY sb.purchase_date;