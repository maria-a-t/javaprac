BEGIN;

INSERT INTO categories(category_id, name) VALUES
  (1, 'Телевизоры'),
  (2, 'Холодильники'),
  (3, 'Стиральные машины'),
  (4, 'DVD-проигрыватели'),
  (5, 'Пылесосы')
ON CONFLICT (category_id) DO NOTHING;

INSERT INTO manufacturers(manufacturer_id, name, country) VALUES
  (1, 'Samsung', 'South Korea'),
  (2, 'LG', 'South Korea'),
  (3, 'Bosch', 'Germany'),
  (4, 'Sony', 'Japan'),
  (5, 'Philips', 'Netherlands')
ON CONFLICT (manufacturer_id) DO NOTHING;

INSERT INTO products(product_id, category_id, manufacturer_id, model_name, description, price, assembly_place, stock_qty, warranty_months) VALUES
  (1, 1, 1, 'Samsung UE55AU7100', 'LED TV 55" 4K', 39990.00, 'Slovakia', 12, 24),
  (2, 1, 4, 'Sony KD-50X80J', 'LED TV 50" 4K', 54990.00, 'Malaysia', 7, 24),
  (3, 2, 2, 'LG GA-B509', 'Two-door fridge with No Frost', 58990.00, 'Poland', 5, 36),
  (4, 2, 3, 'Bosch KGN39', 'No Frost fridge, energy class A+', 76990.00, 'Turkey', 3, 36),
  (5, 3, 3, 'Bosch WAT24440', 'Front-load washer', 52990.00, 'Germany', 6, 24),
  (6, 3, 2, 'LG F2V5GS0W', 'Washer with steam', 47990.00, 'Russia', 9, 24),
  (7, 4, 4, 'Sony DVP-SR760H', 'DVD player with HDMI', 6990.00, 'China', 20, 12),
  (8, 5, 5, 'Philips FC9332', 'Bagless vacuum cleaner', 10990.00, 'China', 15, 24)
ON CONFLICT (product_id) DO NOTHING;

INSERT INTO product_attributes(attribute_id, product_id, attr_name, attr_value) VALUES
  -- TVs
  (1, 1, 'Диагональ', '55"'),
  (2, 1, 'Разрешение', '3840x2160'),
  (3, 1, 'Тип экрана', 'LED'),
  (4, 2, 'Диагональ', '50"'),
  (5, 2, 'Разрешение', '3840x2160'),
  (6, 2, 'Частота', '60 Hz'),

  -- Fridges
  (7, 3, 'Объем холодильной камеры', '277 л'),
  (8, 3, 'Объем морозильной камеры', '107 л'),
  (9, 3, 'No Frost', 'Да'),
  (10, 4, 'Объем холодильной камеры', '279 л'),
  (11, 4, 'Объем морозильной камеры', '87 л'),
  (12, 4, 'Энергопотребление', 'A+'),

  -- Washers
  (13, 5, 'Макс. загрузка', '9 кг'),
  (14, 5, 'Обороты', '1200'),
  (15, 5, 'Энергопотребление', 'A+++'),
  (16, 6, 'Макс. загрузка', '8.5 кг'),
  (17, 6, 'Обороты', '1200'),
  (18, 6, 'Пар', 'Да'),

  -- DVD
  (19, 7, 'Интерфейсы', 'HDMI, USB'),
  (20, 7, 'Форматы', 'DVD, CD, MP3'),
  (21, 7, 'Upscaling', '1080p'),

  -- Vacuum
  (22, 8, 'Тип', 'Без мешка'),
  (23, 8, 'Мощность', '900 Вт'),
  (24, 8, 'Объем пылесборника', '1.5 л')
ON CONFLICT (attribute_id) DO NOTHING;

INSERT INTO customers(customer_id, full_name, phone, email, address) VALUES
  (1, 'Иванов Иван Иванович', '+7-900-111-22-33', 'ivanov@example.com', 'Москва, ул. Пушкина, д. 10'),
  (2, 'Петрова Анна Сергеевна', '+7-900-444-55-66', 'petrova@example.com', 'Санкт-Петербург, Невский пр., д. 25'),
  (3, 'Сидоров Павел Николаевич', '+7-900-777-88-99', 'sidorov@example.com', 'Казань, ул. Баумана, д. 3'),
  (4, 'Смирнова Мария Олеговна', '+7-901-000-11-22', 'smirnova@example.com', 'Екатеринбург, ул. Ленина, д. 15'),
  (5, 'Кузнецов Алексей Дмитриевич', '+7-901-333-44-55', 'kuznetsov@example.com', 'Новосибирск, ул. Советская, д. 8')
ON CONFLICT (customer_id) DO NOTHING;

INSERT INTO order_statuses(status_id, status_name) VALUES
  (1, 'в обработке'),
  (2, 'собран'),
  (3, 'поставлен')
ON CONFLICT (status_id) DO NOTHING;

INSERT INTO orders(order_id, customer_id, status_id, created_at, total_cost) VALUES
  (1, 1, 1, TIMESTAMP '2026-02-20 10:15:00', 46980.00),
  (2, 2, 2, TIMESTAMP '2026-02-21 12:40:00', 58990.00),
  (3, 3, 3, TIMESTAMP '2026-02-22 18:05:00', 58980.00),
  (4, 4, 1, TIMESTAMP '2026-02-23 09:30:00', 10990.00)
ON CONFLICT (order_id) DO NOTHING;

INSERT INTO order_items(order_item_id, order_id, product_id, quantity, unit_price, line_total) VALUES
  (1, 1, 1, 1, 39990.00, 39990.00),
  (2, 1, 7, 1, 6990.00, 6990.00),
  (3, 2, 3, 1, 58990.00, 58990.00),
  (4, 3, 6, 1, 47990.00, 47990.00),
  (5, 3, 8, 1, 10990.00, 10990.00),
  (6, 4, 8, 1, 10990.00, 10990.00)
ON CONFLICT (order_item_id) DO NOTHING;

INSERT INTO delivery(delivery_id, order_id, address, time_from, time_to) VALUES
  (1, 1, 'Москва, ул. Пушкина, д. 10', TIMESTAMP '2026-02-21 12:00:00', TIMESTAMP '2026-02-21 16:00:00'),
  (2, 2, 'Санкт-Петербург, Невский пр., д. 25', TIMESTAMP '2026-02-22 10:00:00', TIMESTAMP '2026-02-22 14:00:00'),
  (3, 3, 'Казань, ул. Баумана, д. 3', TIMESTAMP '2026-02-23 15:00:00', TIMESTAMP '2026-02-23 19:00:00'),
  (4, 4, 'Екатеринбург, ул. Ленина, д. 15', TIMESTAMP '2026-02-24 09:00:00', TIMESTAMP '2026-02-24 12:00:00')
ON CONFLICT (delivery_id) DO NOTHING;

SELECT setval('categories_category_id_seq', (SELECT COALESCE(MAX(category_id),0) FROM categories));
SELECT setval('manufacturers_manufacturer_id_seq', (SELECT COALESCE(MAX(manufacturer_id),0) FROM manufacturers));
SELECT setval('products_product_id_seq', (SELECT COALESCE(MAX(product_id),0) FROM products));
SELECT setval('product_attributes_attribute_id_seq', (SELECT COALESCE(MAX(attribute_id),0) FROM product_attributes));
SELECT setval('customers_customer_id_seq', (SELECT COALESCE(MAX(customer_id),0) FROM customers));
SELECT setval('order_statuses_status_id_seq', (SELECT COALESCE(MAX(status_id),0) FROM order_statuses));
SELECT setval('orders_order_id_seq', (SELECT COALESCE(MAX(order_id),0) FROM orders));
SELECT setval('order_items_order_item_id_seq', (SELECT COALESCE(MAX(order_item_id),0) FROM order_items));
SELECT setval('delivery_delivery_id_seq', (SELECT COALESCE(MAX(delivery_id),0) FROM delivery));

COMMIT;