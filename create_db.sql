DROP TABLE "carts";
DROP TABLE "line_items";
DROP TABLE "orders";
DROP TABLE "products";
DROP TABLE "users";

CREATE TABLE "carts" (id INTEGER PRIMARY KEY AUTOINCREMENT,
	created_at DEFAULT CURRENT_TIMESTAMP,
	updated_at TEXT(63)
	);

CREATE TABLE "line_items" (id INTEGER PRIMARY KEY AUTOINCREMENT,
	created_at DEFAULT CURRENT_TIMESTAMP,
	updated_at TEXT(63),
	product_id INTEGER,
	cart_id INTEGER,
	order_id INTEGER,
	quantity INTEGER DEFAULT 1,
	price REAL
	);

CREATE TABLE "orders" (id INTEGER PRIMARY KEY AUTOINCREMENT,
	created_at DEFAULT CURRENT_TIMESTAMP,
	updated_at TEXT(63),
	name TEXT,
	address TEXT,
	email TEXT,
	pay_type TEXT
	);

CREATE TABLE "products" (id INTEGER PRIMARY KEY AUTOINCREMENT,
	created_at DEFAULT CURRENT_TIMESTAMP,
	updated_at TEXT(63),
	title TEXT,
	description TEXT,
	image_url TEXT,
	price REAL
	);

CREATE TABLE "users" (id INTEGER PRIMARY KEY AUTOINCREMENT,
	created_at DEFAULT CURRENT_TIMESTAMP,
	updated_at TEXT(63),
	name TEXT,
	password_digest TEXT
	);
