BEGIN;

-- 1. Thêm cột image_url vào bảng product nếu chưa có (Dành cho yêu cầu "có hình ảnh")
ALTER TABLE product ADD COLUMN IF NOT EXISTS image_url VARCHAR(255);

-- Cập nhật hình ảnh cho 3 sản phẩm cũ (tùy chọn để đồng bộ dữ liệu)
UPDATE product SET images = 'https://images.pexels.com/photos/7772548/pexels-photo-7772548.jpeg?auto=compress&cs=tinysrgb&w=600' WHERE id = 1;
UPDATE product SET images= 'https://images.pexels.com/photos/5995816/pexels-photo-5995816.jpeg?auto=compress&cs=tinysrgb&w=600' WHERE id = 2;
UPDATE product SET images = 'https://images.pexels.com/photos/35285814/pexels-photo-35285814.jpeg?auto=compress&cs=tinysrgb&w=600' WHERE id = 3;

-- 2. Thêm các Type Of Product mới về chủ đề quần áo, phụ kiện (Tiếp nối ID từ 4)
INSERT INTO type_of_product (id, type) VALUES
(4, 'Áo'),
(5, 'Quần'),
(6, 'Nón'),
(7, 'Thắt lưng'),
(8, 'Vớ'),
(9, 'Áo khoác')
ON CONFLICT (id) DO NOTHING;

-- 3. Thêm 30 sản phẩm chủ đề Quần Áo & Phụ kiện
INSERT INTO product (id, name, description, price, type_id, images) VALUES
-- Nhóm Áo (type_id = 4)
(4, 'Áo thun nam Basic', 'Áo thun cotton 100% thoáng mát, form regular', 150000.00, 4, 'https://images.pexels.com/photos/9775889/pexels-photo-9775889.jpeg?auto=compress&cs=tinysrgb&w=600'),
(5, 'Áo sơ mi trắng công sở', 'Sơ mi lụa mềm mịn, chống nhăn', 350000.00, 4, 'https://images.pexels.com/photos/13915356/pexels-photo-13915356.jpeg?auto=compress&cs=tinysrgb&w=600'),
(6, 'Áo Polo thể thao', 'Chất liệu cá sấu co giãn 4 chiều', 250000.00, 4, 'https://images.pexels.com/photos/17984122/pexels-photo-17984122.jpeg?auto=compress&cs=tinysrgb&w=600'),
(7, 'Áo Croptop nữ', 'Kiểu dáng năng động, trẻ trung cho phái nữ', 120000.00, 4, 'https://images.pexels.com/photos/20723191/pexels-photo-20723191.jpeg?auto=compress&cs=tinysrgb&w=600'),
(8, 'Áo len tăm tay dài', 'Áo len giữ nhiệt nhẹ nhàng mùa Thu Đông', 220000.00, 4, 'https://images.pexels.com/photos/14642651/pexels-photo-14642651.jpeg?auto=compress&cs=tinysrgb&w=600'),
(9, 'Áo hai dây lụa', 'Phù hợp mặc lót hoặc đi biển', 90000.00, 4, 'https://images.pexels.com/photos/7956102/pexels-photo-7956102.jpeg?auto=compress&cs=tinysrgb&w=600'),
(10, 'Áo hoodie Unisex', 'Có mũ trùm đầu, chất nỉ bông dày dặn', 300000.00, 4, 'https://images.pexels.com/photos/2108816/pexels-photo-2108816.png?auto=compress&cs=tinysrgb&w=600'),

-- Nhóm Quần (type_id = 5)
(11, 'Quần Jeans nam Slimfit', 'Vải denim co giãn, tôn dáng', 450000.00, 5, 'https://images.pexels.com/photos/10133278/pexels-photo-10133278.jpeg?auto=compress&cs=tinysrgb&w=600'),
(12, 'Quần tây âu công sở', 'Quần ống đứng thanh lịch', 400000.00, 5, 'https://images.pexels.com/photos/9464625/pexels-photo-9464625.jpeg?auto=compress&cs=tinysrgb&w=600'),
(13, 'Quần short Kaki', 'Quần short nam mặc nhà và dạo phố', 180000.00, 5, 'https://images.pexels.com/photos/27721736/pexels-photo-27721736.jpeg?auto=compress&cs=tinysrgb&w=600'),
(14, 'Quần Jogger nỉ', 'Phong cách streetwear, bo chun lai', 250000.00, 5, 'https://images.pexels.com/photos/14609477/pexels-photo-14609477.jpeg?auto=compress&cs=tinysrgb&w=600'),
(15, 'Quần Culottes ống rộng', 'Dành cho nữ, chất đũi mát mẻ', 280000.00, 5, 'https://images.pexels.com/photos/31703299/pexels-photo-31703299.jpeg?auto=compress&cs=tinysrgb&w=600'),
(16, 'Quần Legging thể thao', 'Ôm sát, co giãn tốt cho tập gym/yoga', 150000.00, 5, 'https://images.pexels.com/photos/7748773/pexels-photo-7748773.jpeg?auto=compress&cs=tinysrgb&w=600'),

-- Nhóm Nón (type_id = 6)
(17, 'Nón lưỡi trai thêu logo', 'Chất kaki dày dặn, form chuẩn', 100000.00, 6, 'https://images.pexels.com/photos/27401299/pexels-photo-27401299.jpeg?auto=compress&cs=tinysrgb&w=600'),
(18, 'Nón Bucket vành tròn', 'Nón tai bèo phong cách Hàn Quốc', 120000.00, 6, 'https://images.pexels.com/photos/6069764/pexels-photo-6069764.jpeg?auto=compress&cs=tinysrgb&w=600'),
(19, 'Nón len Beanie', 'Giữ ấm mùa đông, phong cách basic', 80000.00, 6, 'https://images.pexels.com/photos/17474801/pexels-photo-17474801.jpeg?auto=compress&cs=tinysrgb&w=600'),
(20, 'Nón rơm đi biển', 'Vành rộng che nắng, đính nơ', 150000.00, 6, 'https://images.pexels.com/photos/18487320/pexels-photo-18487320.jpeg?auto=compress&cs=tinysrgb&w=600'),

-- Nhóm Thắt lưng/Nịt (type_id = 7)
(21, 'Thắt lưng da bò thật', 'Mặt khóa kim loại nguyên khối, chống gỉ', 350000.00, 7, 'https://images.pexels.com/photos/31959214/pexels-photo-31959214.jpeg?auto=compress&cs=tinysrgb&w=600'),
(22, 'Thắt lưng nữ bản nhỏ', 'Dùng phối đầm, váy điệu đà', 85000.00, 7, 'https://images.pexels.com/photos/32498751/pexels-photo-32498751.jpeg?auto=compress&cs=tinysrgb&w=600'),
(23, 'Thắt lưng vải Canvas', 'Kiểu dáng thể thao, khóa nhựa bấm', 70000.00, 7, 'https://images.pexels.com/photos/31367060/pexels-photo-31367060.png?auto=compress&cs=tinysrgb&w=600'),
(24, 'Nịt bụng định hình', 'Đai nịt bụng latex giảm eo', 500000.00, 7, 'https://images.pexels.com/photos/39665766/pexels-photo-39665766.jpeg?auto=compress&cs=tinysrgb&w=600'),

-- Nhóm Vớ (type_id = 8)
(25, 'Set 5 đôi vớ lười', 'Vớ cổ ngắn, có silicon chống tuột gót', 100000.00, 8, 'https://images.pexels.com/photos/10563910/pexels-photo-10563910.jpeg?auto=compress&cs=tinysrgb&w=600'),
(26, 'Vớ thể thao cổ cao', 'Thấm hút mồ hôi, đệm lòng bàn chân dày', 45000.00, 8, 'https://images.pexels.com/photos/10923072/pexels-photo-10923072.jpeg?auto=compress&cs=tinysrgb&w=600'),
(27, 'Vớ len họa tiết Vintage', 'Chất len mềm, họa tiết thổ cẩm', 60000.00, 8, 'https://images.pexels.com/photos/15948085/pexels-photo-15948085.jpeg?auto=compress&cs=tinysrgb&w=600'),
(28, 'Quần tất lưới nữ', 'Mỏng nhẹ, dai, chống rách', 95000.00, 8, 'https://images.pexels.com/photos/4971994/pexels-photo-4971994.jpeg?auto=compress&cs=tinysrgb&w=600'),

-- Nhóm Áo khoác (type_id = 9)
(29, 'Áo khoác Denim Retro', 'Áo bò cổ bẻ phong cách cổ điển', 450000.00, 9, 'https://images.pexels.com/photos/36607477/pexels-photo-36607477.jpeg?auto=compress&cs=tinysrgb&w=600'),
(30, 'Áo khoác gió chống nước', 'Chất liệu dù 2 lớp, đi mưa nhẹ', 320000.00, 9, 'https://images.pexels.com/photos/13085468/pexels-photo-13085468.jpeg?auto=compress&cs=tinysrgb&w=600'),
(31, 'Áo Blazer Hàn Quốc', 'Áo khoác blazer thanh lịch đi làm, đi tiệc', 550000.00, 9, 'https://images.pexels.com/photos/11124953/pexels-photo-11124953.jpeg?auto=compress&cs=tinysrgb&w=600'),
(32, 'Áo khoác phao siêu nhẹ', 'Chần bông giữ ấm tuyệt đối', 650000.00, 9, 'https://images.pexels.com/photos/20645811/pexels-photo-20645811.jpeg?auto=compress&cs=tinysrgb&w=600'),
(33, 'Áo Cardigan len', 'Khoác ngoài nhẹ nhàng cho mùa thu', 280000.00, 9, 'https://images.pexels.com/photos/21777956/pexels-photo-21777956.jpeg?auto=compress&cs=tinysrgb&w=600')
ON CONFLICT (id) DO NOTHING;

-- 4. Cập nhật lại chuỗi tự tăng (Sequence) để tránh lỗi duplicate key sau này
SELECT setval('type_of_product_id_seq', COALESCE((SELECT MAX(id) FROM type_of_product), 1));
SELECT setval('product_id_seq', COALESCE((SELECT MAX(id) FROM product), 1));

COMMIT;
