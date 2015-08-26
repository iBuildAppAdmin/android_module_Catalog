/****************************************************************************
 * *
 * Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 * *
 * This file is part of iBuildApp.                                          *
 * *
 * This Source Code Form is subject to the terms of the iBuildApp License.  *
 * You can obtain one at http://ibuildapp.com/license/                      *
 * *
 ****************************************************************************/
package com.ibuildapp.romanblack.CataloguePlugin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class to interract with DB
 */
public class SqlAdapter {

    private static final String TAG = SqlAdapter.class.getCanonicalName();
    private final static String CATEGORIES = "CATEGORIES";
    private final static String PRODUCTS = "PRODUCTS";
    private final static String SHOPPING_CART = "SHOPPING_CART";
    private final static String USER_PROFILE = "USER_PROFILE";
    private static String appId;
    private static int widgetOrder;
    private static Context context;
    private static String databaseName = "Catalogue.db";
    private static SQLiteDatabase db = null;
    // table names
    private static String[] tableNames = {CATEGORIES, PRODUCTS, SHOPPING_CART, USER_PROFILE};

    /**
     * close DB
     */
    public static void closeDb() {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    /**
     * SQL adapter initialization
     *
     * @param context     - контекст приложения
     * @param appId       - applicatin uid
     * @param widgetOrder - widget uid in app
     */
    public static void init(Context context, String appId, int widgetOrder) {
        SqlAdapter.context = context;
        SqlAdapter.appId = appId;
        SqlAdapter.widgetOrder = widgetOrder;

        if (db == null) {
            db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        }
    }

    /**
     * drop tables
     */
    public static void dropTables() {
        if (db == null) {
            db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        }

        try {
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", createTableName(PRODUCTS)));
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", createTableName(CATEGORIES)));
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", createTableName(SHOPPING_CART)));
            db.execSQL(String.format("DROP TABLE IF EXISTS %s", createTableName(USER_PROFILE)));
        } catch (Exception e) {
        }
    }

    /**
     * create tables
     */
    private static void createTables() {
        dropTables();
        createTableCategories();
        createTableProducts();
        createTableShoppingCart();
        createTableUserProfile();
    }

    /**
     * create categories table
     */
    private static void createTableCategories() {
        String query = String.format("CREATE TABLE %s ", createTableName(CATEGORIES))
                + "( "
                + " ID INTEGER, "
                + " ORDERVAL INTEGER, "
                + " VALID INTEGER, "
                + " IMAGE_URL TEXT, "
                + " IMAGE_RES TEXT, "
                + " IMAGE_PATH TEXT, "
                + " CATEGORY_NAME TEXT, "
                + " VISIBLE INTEGER, "
                + " PARENT_ID INTEGER, "
                + " CONSTRAINT PK_CATEGORIES PRIMARY KEY (ID) "
                + ")";
        db.execSQL(query);
    }

    /**
     * create products table
     */
    private static void createTableProducts() {
        String query = String.format("CREATE TABLE %s ", createTableName(PRODUCTS))
                + "( "
                + " ID INTEGER, "
                + " ITEM_ID INTEGER, "
                + " ORDERVAL INTEGER, "
                + " CATEGORY_ID INTEGER,"
                + " VALID INTEGER, "
                + " VISIBLE INTEGER, "
                + " PRODUCT_NAME TEXT, "
                + " PRODUCT_NAME_LOWER TEXT, "
                + " DESCRIPTION TEXT, "
                + " DESCRIPTION_LOWER TEXT, "
                + " PRICE REAL, "
                + " IMAGE_URL TEXT, "
                + " IMAGE_RES TEXT, "
                + " IMAGE_PATH TEXT, "
                + " THUMBNAIL_URL TEXT, "
                + " THUMBNAIL_RES TEXT, "
                + " THUMBNAIL_PATH TEXT, "
                + " CONSTRAINT PK_PRODUCT PRIMARY KEY (ID) "
                + ")";
        db.execSQL(query);
    }

    /**
     * create shopping cart table
     */
    private static void createTableShoppingCart() {
        String query = String.format("CREATE TABLE %s ", createTableName(SHOPPING_CART))
                + "( "
                + " ID INTEGER PRIMARY KEY, "
                + " COUNT INTEGER "
                + ")";
        db.execSQL(query);
    }

    /**
     * create user profile table
     */
    private static void createTableUserProfile() {
        String query = String.format("CREATE TABLE %s ", createTableName(USER_PROFILE))
                + "( "
                + " ID INTEGER PRIMARY KEY, "
                + " FIRST_NAME TEXT, "
                + " LAST_NAME TEXT, "
                + " EMAIL_ADDRESS TEXT, "
                + " PHONE TEXT, "
                + " COUNTRY TEXT, "
                + " STREET_ADDRESS TEXT, "
                + " CITY TEXT, "
                + " STATE TEXT, "
                + " ZIP_CODE TEXT, "
                + " NOTE TEXT "
                + ")";
        db.execSQL(query);
    }

    /**
     * Check existing of tables
     *
     * @return true if tables exists, false otherwise
     */
    public static boolean isExist() {
        boolean result;

        try {
            if (db == null) {
                db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
            }
            result = existsTable();
        } catch (Exception e) {
            result = false;
        }

        return result;

    }

    /**
     * clear tables
     */
    public static void deleteDataFromTables() {
        if (db == null) {
            db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        }

        try {
            db.delete(createTableName(CATEGORIES), null, null);
            db.delete(createTableName(PRODUCTS), null, null);
//            db.delete(createTableName(SHOPPING_CART), null, null);
//            db.delete(createTableName(USER_PROFILE), null, null);
        } catch (Exception e) {
        }
    }

    /**
     * Check existing of DB and tables inside
     *
     * @return true if DB and tables exists of was created, false otherwise
     */
    private static boolean isExistOrCreate() {
        boolean result = true;

        try {
            if (db == null) {
                if (context == null) {
                    Log.e("SHOPPING", "context == NULL");
                } else if (databaseName == null) {
                    Log.e("SHOPPING", "databaseName == NULL");
                }

                db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
            }

            if (!existsTable()) {
                createTables();
                result = existsTable();
            }
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    /**
     * Check existing of tables
     *
     * @return true if tables exists, false otherwise
     */
    private static boolean existsTable() {
        boolean result = true;

        try {
            for (String tableName : tableNames) {
                result &= existTable(String.format("%s_%s_%d", tableName, appId, widgetOrder));
            }
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    /**
     * Check existing of table with name passed
     *
     * @param tableName table name
     * @return true if table exists, false otherwise
     */
    private static boolean existTable(String tableName) {
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?", new String[]{tableName});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * insert categories
     *
     * @param rows list of categories
     */
    public static void insertCategoryRows(List<CategoryEntity> rows) {
        try {
            if (isExistOrCreate()) {
                ContentValues contentValues = new ContentValues(rows.size());
                for (CategoryEntity entity : rows) {
                    try {
                        fillCategory(contentValues, entity);
                        long result = db.insertWithOnConflict(createTableName(CATEGORIES), "", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } catch (Exception ex) {
                        Log.e(TAG, "");
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * insert products
     *
     * @param rows list of products
     */
    public static void insertProductRows(List<ProductEntity> rows) {
        try {
            if (isExistOrCreate()) {
                ContentValues contentValues = new ContentValues();
                for (ProductEntity entity : rows) {
                    try {
                        fillProduct(contentValues, entity);
                        db.insertWithOnConflict(createTableName(PRODUCTS), "", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } catch (Exception ex) {
                        Log.e(TAG, "");
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * insert shopping cart content
     *
     * @param rows list of shopping cart content
     */
    public static void insertShoppingCartContent(List<ShoppingCart.Product> rows) {
        try {
            if (isExistOrCreate()) {
                ContentValues contentValues = new ContentValues();
                for (ShoppingCart.Product entity : rows) {
                    try {
                        fillShoppingCart(contentValues, entity);
                        db.insertWithOnConflict(createTableName(SHOPPING_CART), "", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } catch (Exception ex) {
                        Log.e(TAG, "");
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * insert user profile
     *
     * @param userProfile user profile
     */
    public static void insertUserProfile(UserProfile userProfile) {
        try {
            if (isExistOrCreate()) {
                ContentValues contentValues = new ContentValues();
                try {
                    fillUserProfile(contentValues, userProfile);
                    db.insertWithOnConflict(createTableName(USER_PROFILE), "", contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                } catch (Exception ex) {
                    Log.e(TAG, "");
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * fill category row
     *
     * @param contentValues row
     * @param entity        entity
     */
    private static void fillCategory(ContentValues contentValues, CategoryEntity entity) {
        contentValues.put("ID", entity.id);
        contentValues.put("VALID", entity.valid);
        contentValues.put("ORDERVAL", entity.order);
        contentValues.put("VISIBLE", entity.visibility);
        contentValues.put("PARENT_ID", entity.parentId);
        contentValues.put("CATEGORY_NAME", entity.name);
        contentValues.put("IMAGE_URL", entity.imageURL);
        contentValues.put("IMAGE_RES", entity.imageRes);
        contentValues.put("IMAGE_PATH", entity.imagePath);
    }

    /**
     * fill product row
     *
     * @param contentValues row
     * @param entity        entity
     */
    private static void fillProduct(ContentValues contentValues, ProductEntity entity) {
        contentValues.put("ID", entity.id);
        contentValues.put("ITEM_ID", entity.item_id);
        contentValues.put("VALID", entity.valid);
        contentValues.put("ORDERVAL", entity.order);
        contentValues.put("CATEGORY_ID", entity.categoryId);
        contentValues.put("PRODUCT_NAME", entity.name);
        contentValues.put("PRODUCT_NAME_LOWER", entity.name.toLowerCase());
        contentValues.put("DESCRIPTION", entity.description);
        contentValues.put("DESCRIPTION_LOWER", entity.description.toLowerCase());
        contentValues.put("VISIBLE", entity.visibility);
        contentValues.put("PRICE", entity.price);
        contentValues.put("IMAGE_URL", entity.imageURL);
        contentValues.put("IMAGE_RES", entity.imageRes);
        contentValues.put("IMAGE_PATH", entity.imagePath);
        contentValues.put("THUMBNAIL_URL", entity.thumbnailURL);
        contentValues.put("THUMBNAIL_RES", entity.thumbnailRes);
        contentValues.put("THUMBNAIL_PATH", entity.thumbnailPath);
    }

    /**
     * fill shopping cart content row
     *
     * @param contentValues row
     * @param entity        entity
     */
    private static void fillShoppingCart(ContentValues contentValues, ShoppingCart.Product entity) {
        contentValues.put("ID", entity.getId());
        contentValues.put("COUNT", entity.getQuantity());
    }

    /**
     * fill user profile row
     *
     * @param contentValues row
     * @param entity        entity
     */
    private static void fillUserProfile(ContentValues contentValues, UserProfile entity) {
        contentValues.put("ID", 1);
        contentValues.put("FIRST_NAME", entity.getFirstName());
        contentValues.put("LAST_NAME", entity.getLastName());
        contentValues.put("EMAIL_ADDRESS", entity.getEmailAddress());
        contentValues.put("PHONE", entity.getPhone());
        contentValues.put("COUNTRY", entity.getCountry());
        contentValues.put("STREET_ADDRESS", entity.getStreetAddress());
        contentValues.put("CITY", entity.getCity());
        contentValues.put("STATE", entity.getState());
        contentValues.put("ZIP_CODE", entity.getZipCode());
        contentValues.put("NOTE", entity.getNote());
    }

    /**
     * Delete categories
     *
     * @param rows list of categories to delete
     */
    public static void deleteCategoryRow(List<CategoryEntity> rows) {
        for (CategoryEntity cat : rows) {
            deleteCategory(cat.id);
        }
    }

    /**
     * Delete category
     *
     * @param id id of category to delete
     */
    public static void deleteCategory(int id) {
        try {
            if (isExistOrCreate()) {
                int rc = db.delete(createTableName(CATEGORIES), "ID = ?", new String[]{String.valueOf(id)});

            }
        } catch (Exception ex) {
        }
    }

    /**
     * Delete products
     *
     * @param row list of products to delete
     */
    public static void deleteProductRow(List<ProductEntity> row) {
        for (ProductEntity prod : row) {
            deleteItemFromProducts(prod.id);
        }
    }

    /**
     * Delete product
     *
     * @param id id of product to delete
     */
    public static void deleteItemFromProducts(int id) {
        try {
            if (isExistOrCreate()) {
                db.delete(createTableName(PRODUCTS), "ID = ?", new String[]{String.valueOf(id)});
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Delete shopping cart content
     *
     * @param row list of shopping cart content to delete
     */
    public static void deleteShoppingCartRow(List<ShoppingCart.Product> row) {

        for (ShoppingCart.Product product : row)
            deleteItemFromShoppingCart(product.getId());
    }

    /**
     * Delete shopping cart item
     *
     * @param id id of shopping cart item to delete
     */
    public static void deleteItemFromShoppingCart(int id) {
        try {
            if (isExistOrCreate()) {
                db.delete(createTableName(SHOPPING_CART), "ID = ?", new String[]{String.valueOf(id)});
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Delete user profile
     */
    public static void deleteUserProfile() {
        try {
            if (isExistOrCreate()) {
                db.delete(createTableName(USER_PROFILE), "ID = ?", new String[]{"1"});
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Select categories from DB with parent id
     *
     * @param parentId parent id
     * @return list of categories
     * @throws IllegalArgumentException
     */
    public static List<CategoryEntity> selectCategoryWithParendId(int parentId) throws IllegalArgumentException {
        List<CategoryEntity> result = new ArrayList<CategoryEntity>();

        if (parentId < 0) {
            throw new IllegalArgumentException("Parent id must be great or equal 0");
        }

        try {
            if (!isExistOrCreate()) {
                return new ArrayList<CategoryEntity>();
            }

            Cursor cursor = db.query(createTableName(CATEGORIES), null, "PARENT_ID = ? AND VISIBLE = ?",
                    new String[]{String.valueOf(parentId), "1"},
                    null,
                    null,
                    "ORDERVAL",
                    null);

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<CategoryEntity>();
            }

            result = new ArrayList<CategoryEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseCategory(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = new ArrayList<CategoryEntity>();
        }
        return result;
    }

    /**
     * Select products from DB like passed
     *
     * @param like search criteria
     * @return list of products
     */
    public static List<ProductEntity> selectProductsLike(String like) {
        List<ProductEntity> result = null;

        if (TextUtils.isEmpty(like)) {
            throw new IllegalArgumentException("Search string is empty");
        }

        try {
            if (!isExistOrCreate()) {
                return new ArrayList<ProductEntity>();
            }

            String query = String.format("SELECT * FROM %s WHERE (PRODUCT_NAME_LOWER LIKE \'%%%s%%\' OR DESCRIPTION_LOWER LIKE '%%%s%%') AND VISIBLE=1 AND VALID=1 ORDER BY PRODUCT_NAME_LOWER", createTableName(PRODUCTS), like.toLowerCase(), like.toLowerCase());
            Cursor cursor = db.rawQuery(query, new String[]{});

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ProductEntity>();
            }

            result = new ArrayList<ProductEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseProduct(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            result = new ArrayList<ProductEntity>();
        }
        return result;
    }

    /**
     * Select products of category from DB with category id
     *
     * @param categoryId category id
     * @return list of categories
     */
    public static List<ProductEntity> selectProductsForCategory(int categoryId) {
        List<ProductEntity> result = null;

        if (categoryId < 0) {
            throw new IllegalArgumentException("Category id must be great or equal 0");
        }

        try {
            if (!isExistOrCreate()) {
                return new ArrayList<ProductEntity>();
            }

            Cursor cursor = db.query(createTableName(PRODUCTS), null, "CATEGORY_ID = ? AND VISIBLE = ?", new String[]{String.valueOf(categoryId), "1"}, null, null, "ORDERVAL");

            if (cursor == null || cursor.getCount() <= 0) {
                return new ArrayList<ProductEntity>();
            }

            result = new ArrayList<ProductEntity>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    result.add(parseProduct(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            result = new ArrayList<ProductEntity>();
        }
        return result;
    }

    /**
     * Select products from DB with product id
     *
     * @param productId product id
     * @return list of products
     */
    public static ProductEntity selectProductById(int productId) {
        ProductEntity result = null;

        if (productId < 0) {
            throw new IllegalArgumentException("Category id must be great or equal 0");
        }

        try {
            if (!isExistOrCreate()) {
                return null;
            }

            Cursor cursor = db.query(createTableName(PRODUCTS), null, "ID = ?", new String[]{String.valueOf(productId)}, null, null, null, null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    result = parseProduct(cursor);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * Update product
     *
     * @param entity entity
     */
    public static void updateProduct(ProductEntity entity) {
        ContentValues contentValues = new ContentValues();
        fillProduct(contentValues, entity);
        db.update(createTableName(PRODUCTS), contentValues, "ID = ?", new String[]{String.valueOf(entity.id)});
    }

    /**
     * Update category
     *
     * @param entity entity
     */
    public static void updateCategory(CategoryEntity entity) {
        ContentValues contentValues = new ContentValues();
        fillCategory(contentValues, entity);
        db.update(createTableName(CATEGORIES), contentValues, "ID = ?", new String[]{String.valueOf(entity.id)});
    }

    /**
     * Select category from DB with category id
     *
     * @param categoryId category id
     * @return category
     */
    public static CategoryEntity selectCategoryById(int categoryId) {
        CategoryEntity result = null;

        if (categoryId < 0) {
            throw new IllegalArgumentException("Category id must be great then 0");
        }

        try {
            if (!isExistOrCreate()) {
                return null;
            }

            Cursor cursor = db.query(createTableName(CATEGORIES), null, "ID = ?", new String[]{String.valueOf(categoryId)}, null, null, null, null);

            if (cursor == null || cursor.getCount() <= 0) {
                return null;
            }

            if (cursor.moveToFirst()) {
                do {
                    result = parseCategory(cursor);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * Select all products
     *
     * @return list of products
     */
    public static List<ProductEntity> selectProducts() {
        List<ProductEntity> result = null;

        if (!isExistOrCreate()) {
            return new ArrayList<ProductEntity>();
        }

        Cursor cursor;
        cursor = db.query(createTableName(PRODUCTS), null, null, null, null, null, null);

        if (cursor == null) {
            return new ArrayList<ProductEntity>();
        }

        if (cursor.getCount() <= 0) {
            return new ArrayList<ProductEntity>();
        }


        result = new ArrayList<ProductEntity>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                result.add(parseProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * Select all categories
     *
     * @return list of categories
     */
    public static List<CategoryEntity> selectCategories() {
        List<CategoryEntity> result = null;

        if (!isExistOrCreate()) {
            return new ArrayList<CategoryEntity>();
        }

        Cursor cursor;
        cursor = db.query(createTableName(CATEGORIES), null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return new ArrayList<CategoryEntity>();
        }

        result = new ArrayList<CategoryEntity>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                result.add(parseCategory(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * Select all shopping cart content
     *
     * @return list of shopping cart content
     */
    public static List<ShoppingCart.Product> selectShoppingCartProducts() {
        List<ShoppingCart.Product> result = null;

        if (!isExistOrCreate()) {
            return new ArrayList<ShoppingCart.Product>();
        }

        Cursor cursor = db.query(createTableName(SHOPPING_CART), null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return new ArrayList<ShoppingCart.Product>();
        }

        result = new ArrayList<ShoppingCart.Product>(cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                result.add(parseShoppingCartProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return result;
    }

    /**
     * Parses product from cursor
     *
     * @param cursor cursor
     * @return entity
     */
    private static ProductEntity parseProduct(Cursor cursor) {
        ProductEntity entity = new ProductEntity();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("ID")) {
                entity.id = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("ITEM_ID")) {
                entity.item_id = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("VISIBLE")) {
                entity.visibility = cursor.getInt(i) == 1;
            } else if (cursor.getColumnName(i).equals("VALID")) {
                entity.valid = cursor.getInt(i) != 0;
            } else if (cursor.getColumnName(i).equals("PRODUCT_NAME")) {
                entity.name = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("DESCRIPTION")) {
                entity.description = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("PRICE")) {
                entity.price = cursor.getFloat(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_URL")) {
                entity.imageURL = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_RES")) {
                entity.imageRes = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_PATH")) {
                entity.imagePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("THUMBNAIL_URL")) {
                entity.thumbnailURL = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("THUMBNAIL_RES")) {
                entity.thumbnailRes = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("THUMBNAIL_PATH")) {
                entity.thumbnailPath = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("CATEGORY_ID")) {
                entity.categoryId = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("ORDERVAL")) {
                entity.order = cursor.getInt(i);
            }
        }
        return entity;
    }
//    + " PRODUCT_NAME TEXT, "
//            + " PRODUCT_NAME_LOWER TEXT, "
//            + " DESCRIPTION TEXT, "
//            + " DESCRIPTION_LOWER TEXT, "

    /**
     * Parses category from cursor
     *
     * @param cursor cursor
     * @return entity
     */
    private static CategoryEntity parseCategory(Cursor cursor) {
        CategoryEntity entity = new CategoryEntity();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("ID")) {
                entity.id = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("VALID")) {
                entity.valid = cursor.getInt(i) != 0;
            } else if (cursor.getColumnName(i).equals("PARENT_ID")) {
                entity.parentId = cursor.getInt(i);
            } else if (cursor.getColumnName(i).equals("VISIBILITY")) {
                entity.visibility = cursor.getInt(i) != 0;
            } else if (cursor.getColumnName(i).equals("CATEGORY_NAME")) {
                entity.name = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_URL")) {
                entity.imageURL = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_RES")) {
                entity.imageRes = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("IMAGE_PATH")) {
                entity.imagePath = cursor.getString(i);
            } else if (cursor.getColumnName(i).equals("VISIBLE")) {
                entity.visibility = cursor.getInt(i) == 1;
            } else if (cursor.getColumnName(i).equals("ORDERVAL")) {
                entity.order = cursor.getInt(i);
            }
        }
        return entity;
    }

    /**
     * Parses shopping cart item from cursor
     *
     * @param cursor cursor
     * @return entity
     */
    private static ShoppingCart.Product parseShoppingCartProduct(Cursor cursor) {
        ShoppingCart.Product.Builder productBuilder = new ShoppingCart.Product.Builder();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("ID")) {
                productBuilder.setId(cursor.getInt(i));
            } else if (cursor.getColumnName(i).equals("COUNT")) {
                productBuilder.setQuantity(cursor.getInt(i));
            }
        }

        return productBuilder.build();
    }

    /**
     * Parses user profile from cursor
     *
     * @param cursor cursor
     * @return entity
     */
    private static UserProfile parseUserProfile(Cursor cursor) {
        UserProfile.Builder userProfileBuilder = new UserProfile.Builder();

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            if (cursor.getColumnName(i).equals("FIRST_NAME")) {
                userProfileBuilder.setFirstName(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("LAST_NAME")) {
                userProfileBuilder.setLastName(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("EMAIL_ADDRESS")) {
                userProfileBuilder.setEmailAddress(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("PHONE")) {
                userProfileBuilder.setPhone(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("COUNTRY")) {
                userProfileBuilder.setCountry(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("STREET_ADDRESS")) {
                userProfileBuilder.setStreetAddress(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("CITY")) {
                userProfileBuilder.setCity(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("STATE")) {
                userProfileBuilder.setState(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("ZIP_CODE")) {
                userProfileBuilder.setZipCode(cursor.getString(i));
            } else if (cursor.getColumnName(i).equals("NOTE")) {
                userProfileBuilder.setNote(cursor.getString(i));
            }
        }

        return userProfileBuilder.build();
    }

    /**
     * Create valid name for table
     *
     * @param tableName table name
     * @return valid table name
     */
    private static String createTableName(String tableName) {
        return String.format("%s_%s_%d", tableName, appId, widgetOrder);
    }

    /**
     * Get user profile
     *
     * @return user profile
     */
    public static UserProfile getUserProfile() {
        UserProfile userProfile = null;

        try {
            if (!isExistOrCreate())
                return new UserProfile.Builder().build();

            Cursor cursor = db.query(createTableName(USER_PROFILE), null, "ID = ?", new String[]{"1"}, null, null, null, null);

            if (cursor == null || cursor.getCount() <= 0)
                return new UserProfile.Builder().build();

            if (cursor.moveToFirst())
                userProfile = parseUserProfile(cursor);

            cursor.close();
        } catch (Exception e) {
            return new UserProfile.Builder().build();
        }

        return userProfile;
    }
}
