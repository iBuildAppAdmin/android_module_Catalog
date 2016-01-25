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
package com.ibuildapp.romanblack.CataloguePlugin.xml;

import android.graphics.Color;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.ibuildapp.romanblack.CataloguePlugin.Statics;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.PaymentData;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductItemType;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCartFields;
import com.ibuildapp.romanblack.CataloguePlugin.model.UIConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * This class pars build-in xml
 */
public class XmlParser {

    private final String TAG = XmlParser.class.getCanonicalName();
    private String xmlStream;
    private UIConfig uiConfig = new UIConfig();
    private ArrayList<CategoryEntity> categoryList = new ArrayList<CategoryEntity>();
    private ArrayList<ProductEntity> productList = new ArrayList<ProductEntity>();
    private CategoryEntity tempCategory;
    private ProductEntity tempProduct;
    private PaymentData paymentData;
    private ShoppingCartFields.Field tempField;

    /**
     * Constructs new XmlParser instance.
     *
     * @param xmlString - module xml data to parse
     */
    public XmlParser(String xmlString) {
        this.xmlStream = xmlString;
    }

    /**
     * Parses module data that was set in constructor with checking xml dependencies
     */
    public void parser() {
        parser(true);
    }

    /**
     * Parses module data that was set in constructor
     *
     * @param checkRequirement - enable or disable checking xml dependencies
     */
    public void parser(boolean checkRequirement) {
        RootElement root = new RootElement("data");
        Element styleElement = root.getChild("config");
        Element categoryElement = root.getChild("categories");
        Element productElement = root.getChild("items");
        Element paymentDataElement = root.getChild("payment_data");
        Element shoppingCart = checkRequirement ? styleElement.requireChild("shoppingcart") : styleElement.getChild("shoppingcart");
        Element shopingCartDescription = shoppingCart.getChild("cartdescription");
        Element orderConfirmation = shoppingCart.getChild("orderconfirmation");
        Element orderForm = shoppingCart.getChild("orderform");
        Statics.isShoppingCartPayPalBased = true;
        Statics.isBasket = true;

        styleElement.getChild("currency").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                uiConfig.currency = body.trim();
            }
        });
        styleElement.getChild("data_host").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                uiConfig.url = body.trim();
                return;
            }
        });
        styleElement.getChild("enabled_buttons").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    Integer res = Integer.valueOf(body.trim());
                    int a1 = res % 10;
                    int a2 = res / 10;
                    boolean value1;
                    if (a1 > 0)
                        value1 = true;
                    else value1 = false;

                    boolean value2;
                    if (a2 > 0)
                        value2 = true;
                    else value2 = false;

                    uiConfig.showLikeButton = value1;
                    uiConfig.showShareButton = value2;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        styleElement.getChild("mainpagestyle").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                uiConfig.mainpagestyle = body.trim();
            }
        });

        orderForm.getChild("firstname").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("firstname").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.firstName = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("firstname").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("lastname").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("lastname").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.lastName = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("lastname").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("street").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("street").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.streetAddress = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("street").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("city").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("city").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.city = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("city").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("country").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("country").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.country = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("country").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("state").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });
        orderForm.getChild("state").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.state = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("state").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("zip").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });
        orderForm.getChild("zip").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.zipCode = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("zip").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("phone").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });
        orderForm.getChild("phone").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.phone = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("phone").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("email").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempField = parserShoppingCartContactFields(attributes);
            }
        });
        orderForm.getChild("email").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                Statics.shoppingCartFields.emailAddress = tempField;
                tempField = null;
            }
        });

        orderForm.getChild("email").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                Statics.isShoppingCartPayPalBased = false;
                tempField = new ShoppingCartFields.Field.Builder()
                        .setVisible(tempField.isVisible())
                        .setRequired(tempField.isRequired())
                        .setMultiline(tempField.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        orderForm.getChild("note").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                Statics.shoppingCartFields.note = parserShoppingCartContactFields(attributes);
            }
        });

        orderForm.getChild("note").getChild("label").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                Statics.shoppingCartFields.note = new ShoppingCartFields.Field.Builder()
                        .setVisible(Statics.shoppingCartFields.note.isVisible())
                        .setRequired(Statics.shoppingCartFields.note.isRequired())
                        .setMultiline(Statics.shoppingCartFields.note.isMultiline())
                        .setHint(s.trim())
                        .build();
            }
        });

        shopingCartDescription.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                Statics.shoppingCartFields.description = body.trim();
            }
        });
        orderConfirmation.getChild("orderTitle").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                Statics.shoppingCartFields.orderTitle = s.trim();
            }
        });

        orderConfirmation.getChild("orderText").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                Statics.shoppingCartFields.orderText = s.trim();
            }
        });

        styleElement.getChild("colorskin").getChild("color1").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color1 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color2").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color2 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color3").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color3 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color4").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color4 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color5").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color5 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color6").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color6 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color7").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color7 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("color8").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.color8 = Color.parseColor(body.trim());
                } catch (Exception e) {
                }
            }
        });

        styleElement.getChild("colorskin").getChild("isLight").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                try {
                    uiConfig.colorSkin.isLight = "1".equals(body.trim());
                } catch (Exception e) {
                }
            }
        });


        // *************************************************************************************************************
        // *************************************************************************************************************
        // *************************************************************************************************************

        categoryElement.getChild("category").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempCategory = parserCategoryAttribute(attributes);
            }
        });

        categoryElement.getChild("category").getChild("categoryname").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempCategory.name = s.trim();
            }
        });

        categoryElement.getChild("category").getChild("categoryimg").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempCategory.imageURL = s.trim();
            }
        });

        categoryElement.getChild("category").getChild("categoryimg_res").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempCategory.imageRes = s.trim();
            }
        });

        categoryElement.getChild("category").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                categoryList.add(tempCategory);
                tempCategory = null;
            }
        });

        // *************************************************************************************************************
        // *************************************************************************************************************
        // *************************************************************************************************************

        productElement.getChild("item").setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                tempProduct = parserItemAttribute(attributes);
            }
        });

        productElement.getChild("item").getChild("itemname").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempProduct.name = s.trim();
            }
        });

        productElement.getChild("item").getChild("itemdescription").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                tempProduct.description = s.trim();
            }
        });

        productElement.getChild("item").getChild("itemprice").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                try {
                    tempProduct.price = Float.parseFloat(s.trim());
                } catch (NumberFormatException e) {
                    tempProduct.price = 0;
                }
            }
        });

        productElement.getChild("item").getChild("images").getChild("image").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.imageUrls.add(body.trim());
            }
        });
        productElement.getChild("item").getChild("itemoldprice").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                try {
                    tempProduct.oldprice = Float.parseFloat(s.trim());
                } catch (NumberFormatException e) {
                    tempProduct.oldprice = -1;
                }
            }
        });

        productElement.getChild("item").getChild("itemtype").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                try {
                    tempProduct.itemType = ProductItemType.valueOf(s.trim().toUpperCase());
                } catch (NumberFormatException e) {
                }
            }
        });

        productElement.getChild("item").getChild("itemurl").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                try {
                    tempProduct.itemUrl = s.trim();
                } catch (NumberFormatException e) {
                }
            }
        });

        productElement.getChild("item").getChild("itembuttontext").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                try {
                    tempProduct.itemButtonText = s.trim();
                } catch (NumberFormatException e) {
                }
            }
        });

        productElement.getChild("item").getChild("image").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.imageURL = body.trim();
            }
        });

        productElement.getChild("item").getChild("image_res").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.imageRes = body.trim();
            }
        });

        productElement.getChild("item").getChild("thumbnail").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.thumbnailURL = body.trim();
            }
        });

        productElement.getChild("item").getChild("thumbnail_res").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.thumbnailRes = body.trim();
            }
        });

        productElement.getChild("item").getChild("itemsku").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                tempProduct.sku = body.trim();
            }
        });

        productElement.getChild("item").setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                productList.add(tempProduct);
                tempProduct = null;
            }
        });

        // *************************************************************************************************************
        // *************************************************************************************************************
        // *************************************************************************************************************

        paymentDataElement.getChild("paypal").getChild("client_id").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                paymentData = new PaymentData.Builder()
                        .setClientId(body.trim())
                        .build();
            }
        });

        // *************************************************************************************************************
        // *************************************************************************************************************
        // *************************************************************************************************************

        try {
            Xml.parse(xmlStream, root.getContentHandler());
        } catch (SAXException e) {
            Log.d(TAG, "", e);
            parser(false);
            Statics.isShoppingCartPayPalBased = false;
            Statics.isBasket = false;
        }
    }

    /**
     * Parses attributes of category
     *
     * @param attributes - attributes
     */
    private CategoryEntity parserCategoryAttribute(Attributes attributes) {
        CategoryEntity tempEntity = new CategoryEntity();

        if (!TextUtils.isEmpty(attributes.getValue("id"))) {
            tempEntity.id = Integer.decode(attributes.getValue("id"));
        } else {
            tempEntity.id = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("order"))) {
            try {
                tempEntity.order = Integer.decode(attributes.getValue("order"));
            } catch (NumberFormatException e) {
                tempEntity.order = -1;
            }
        } else {
            tempEntity.order = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("parentid"))) {
            tempEntity.parentId = Integer.decode(attributes.getValue("parentid"));
        } else {
            tempEntity.parentId = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("valid"))) {
            if (attributes.getValue("valid").compareToIgnoreCase("1") == 0) {
                tempEntity.valid = true;
            } else {
                tempEntity.valid = false;
            }
        } else {
            tempEntity.valid = true;
        }

        if (!TextUtils.isEmpty(attributes.getValue("visible"))) {
            if (attributes.getValue("visible").compareToIgnoreCase("1") == 0) {
                tempEntity.visibility = true;
            } else {
                tempEntity.valid = false;
            }
        } else {
            tempEntity.valid = true;
        }


        return tempEntity;
    }

    /**
     * Parses attributes of item
     *
     * @param attributes - attributes
     * @return parsed entity
     */
    private ProductEntity parserItemAttribute(Attributes attributes) {
        ProductEntity tempEntity = new ProductEntity();

        if (!TextUtils.isEmpty(attributes.getValue("id"))) {
            tempEntity.id = Integer.decode(attributes.getValue("id"));
        } else {
            tempEntity.id = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("pid"))) {
            tempEntity.item_id = Long.decode(attributes.getValue("pid"));
        } else {
            tempEntity.item_id = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("order"))) {
            try {
                tempEntity.order = Integer.decode(attributes.getValue("order"));
            } catch (NumberFormatException e) {
                tempEntity.order = -1;
            }
        } else {
            tempEntity.order = -1;
        }

        if (!TextUtils.isEmpty(attributes.getValue("categoryid"))) {
            tempEntity.categoryId = Integer.parseInt(attributes.getValue("categoryid"));
        }

        if (!TextUtils.isEmpty(attributes.getValue("valid"))) {
            if (attributes.getValue("valid").compareToIgnoreCase("1") == 0) {
                tempEntity.valid = true;
            } else {
                tempEntity.valid = false;
            }
        } else {
            tempEntity.valid = true;
        }

        if (!TextUtils.isEmpty(attributes.getValue("visible"))) {
            if (attributes.getValue("visible").compareToIgnoreCase("1") == 0) {
                tempEntity.visibility = true;
            } else {
                tempEntity.visibility = false;
            }
        } else {
            tempEntity.visibility = true;
        }

        return tempEntity;
    }

    /**
     * Parses attributes of shopping cart fields
     *
     * @param attributes - attributes
     * @return parsed entity
     */
    private ShoppingCartFields.Field parserShoppingCartContactFields(Attributes attributes) {
        ShoppingCartFields.Field.Builder fieldBuilder = new ShoppingCartFields.Field.Builder();

        String visibleAttribute = attributes.getValue("visible");
        fieldBuilder.setVisible(!(visibleAttribute != null && visibleAttribute.length() != 0) || visibleAttribute.compareToIgnoreCase("1") == 0);

        String requiredAttribute = attributes.getValue("required");
        fieldBuilder.setRequired(!(requiredAttribute != null && requiredAttribute.length() != 0) || requiredAttribute.compareToIgnoreCase("1") == 0);

        String multilineAttribute = attributes.getValue("multiline");
        fieldBuilder.setMultiline(!(multilineAttribute != null && multilineAttribute.length() != 0) || multilineAttribute.compareToIgnoreCase("1") == 0);

        return fieldBuilder.build();
    }

    /**
     * @return list of categories which was parsed
     */
    public ArrayList<CategoryEntity> getCategoryList() {
        return categoryList;
    }

    /**
     * @return list of products which was parsed
     */
    public ArrayList<ProductEntity> getProductList() {
        return productList;
    }

    /**
     * @return user interface configuration which was parsed
     */
    public UIConfig getUiConfig() {
        return uiConfig;
    }

    /**
     * @return user payment data which was parsed
     */
    public PaymentData getPaymentData() {
        return paymentData;
    }
}
