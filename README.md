# android module Catalog
Catalog module is intended for displaying of n-level catalog. All elements can be separated using categories. Categories can be displayed in list view or grid view.

**XML Structure declaration**

Tags:
- title - widget name. Title is being displayed on navigation panel when widget is launched.
- colorskin - this is root tag to set up color scheme. Contains 5 elements (color[1-5]). Each widget may set colors for elements of the interface using the color scheme in any order, however generally color1 - background color, color3 - titles color, color4 - font color, color5 - date or price color.
- app_name - name of mobile application, is being added into text message using Share feature.
- currency - string representation of a currency.
- currency_position - arrangement of the name or currency badge concerning the sum (number). Values: left and right
- mainpagestyle - category view type: list or grid. Values: list and grid
- showimages - attribute for images and categories displaying/hiding. Values: on and off.

Example:


    <data>
    <app_name>TestApp</app_name>
    <title><![CDATA[ Products ]]></title>
    <currency><![CDATA[ lei ]]></currency>
    <currencyposition><![CDATA[ right ]]></currencyposition>
    <mainpagestyle><![CDATA[ grid ]]></mainpagestyle>
    <showimages><![CDATA[ on ]]></showimages>
    <colorskin>
        <color1><![CDATA[ #23660f ]]></color1>
        <color2><![CDATA[ #fbff94 ]]></color2>
        <color3><![CDATA[ #b7ffa2 ]]></color3>
        <color4><![CDATA[ #ffffff ]]></color4>
        <color5><![CDATA[ #fbff94 ]]></color5>
    </colorskin>
    <category>
        <categoryname>
            <![CDATA[ New_Category in two lines ]]>
        </categoryname>
        <categoryimg>
            <![CDATA[ ]]>
        </categoryimg>
        <categoryimg_res>
            <![CDATA[ ]]>
        </categoryimg_res>
        <item price="100">
            <itemname>
                <![CDATA[ Item 1 ]]>
            </itemname>
            <itemdescription>
                <![CDATA[
                <font color="#0000bb"><u>test </u><i>test</i> <b>test</b></font>
                ]]>
            </itemdescription>
            <itemimg>
                <![CDATA[
                http://ibuildapp.com/assets2/data/00088/88388/535410/directory/1385544595.jpg
                ]]>
            </itemimg>
            <itemimg_res>
                <![CDATA[ 9e1366f28f82ba3c0ae295691b8920eb904044a0.dat ]]>
            </itemimg_res>
            <itemthumbnail>
                <![CDATA[
                http://ibuildapp.com/getimage.php?width=160&height=160&url=assets2/data/00088/88388/535410/directory/1385544595.jpg
                ]]>
            </itemthumbnail>
            <itemthumbnail_res>
                <![CDATA[ 8fe5ce4c491199a5bc6b26e938790bbfd4e986f0.dat ]]>
            </itemthumbnail_res>
        </item>
    </category>
    <category>
        <categoryname>
            <![CDATA[ Category 2 ]]>
        </categoryname>
        <categoryimg>
            <![CDATA[
            http://ibuildapp.com/assets2/data/00088/88388/535410/directory/1385544680.jpg
            ]]>
        </categoryimg>
        <categoryimg_res>
            <![CDATA[ b181fc18016655b63a08441e1a6f55480435e1ec.dat ]]>
        </categoryimg_res>
        <item price="">
            <itemname>
                <![CDATA[ Item 1 ]]>
            </itemname>
            <itemdescription>
                <![CDATA[ test ]]>
            </itemdescription>
            <itemimg>
                <![CDATA[ ]]>
            </itemimg>
            <itemimg_res>
                <![CDATA[ ]]>
            </itemimg_res>
            <itemthumbnail>
                <![CDATA[ ]]>
            </itemthumbnail>
            <itemthumbnail_res>
                <![CDATA[ ]]>
            </itemthumbnail_res>
        </item>
        <item price="100000000">
            <itemname>
                <![CDATA[ Item 2 ]]>
            </itemname>
            <itemdescription>
                <![CDATA[ <br> ]]>
            </itemdescription>
            <itemimg>
                <![CDATA[
                http://ibuildapp.com/assets2/data/00088/88388/535410/directory/1385544760.jpg
                ]]>
            </itemimg>
            <itemimg_res>
                <![CDATA[ c6e3ae27f1e5b81f2b1135e998af43b7f7e2a635.dat ]]>
            </itemimg_res>
            <itemthumbnail>
                <![CDATA[
                http://ibuildapp.com/getimage.php?width=160&height=160&url=assets2/data/00088/88388/535410/directory/1385544760.jpg
                ]]>
            </itemthumbnail>
            <itemthumbnail_res>
                <![CDATA[ a5d7710bed2476453300a1d8e16cc910338ebf0f.dat ]]>
            </itemthumbnail_res>
        </item>
    </category>
    </data>
