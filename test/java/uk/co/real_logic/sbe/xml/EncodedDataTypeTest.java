/* -*- mode: java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil -*- */
/*
 * Copyright 2013 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.sbe.xml;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.real_logic.sbe.PrimitiveType;
import uk.co.real_logic.sbe.SbeTool;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Boolean.valueOf;
import static java.lang.Integer.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.co.real_logic.sbe.PrimitiveValue.parse;

public class EncodedDataTypeTest
{
    @Test
    public void shouldHandleSettingAllAttributes()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType\" presence=\"required\" primitiveType=\"char\" length=\"1\" variableLength=\"false\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        Type t = map.get("testType");
        assertThat(t.getName(), is("testType"));
        assertThat(t.getPresence(), is(Presence.REQUIRED));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(d.getPrimitiveType(), is(PrimitiveType.CHAR));
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(Boolean.FALSE));
    }

    @Test
    public void shouldHandleMultipleTypes()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType1\" presence=\"required\" primitiveType=\"char\" length=\"1\" variableLength=\"false\"/>" +
            "    <type name=\"testType2\" presence=\"required\" primitiveType=\"int8\" length=\"1\" variableLength=\"false\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        assertThat(valueOf(map.size()), is(valueOf(2)));
        assertThat(map.get("testType1").getName(), is("testType1"));
        assertThat(map.get("testType2").getName(), is("testType2"));
    }

    @Test
    public void shouldSetAppropriateDefaultsWhenNoneSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        // assert that testType is in map and name of Type is correct
        assertThat(map.get("testType").getName(), is("testType"));
        // assert defaults for length, variableLength and presence
        Type t = map.get("testType");
        assertThat(t.getPresence(), is(Presence.REQUIRED));
        EncodedDataType d = (EncodedDataType)t;
        assertThat(valueOf(d.getLength()), is(valueOf(1)));
        assertThat(valueOf(d.getVariableLength()), is(Boolean.FALSE));
    }

    @Test
    public void shouldUseAppropriatePresence()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeDefault\" primitiveType=\"char\"/>" +
            "    <type name=\"testTypeRequired\" presence=\"required\" primitiveType=\"char\"/>" +
            "    <type name=\"testTypeOptional\" presence=\"optional\" primitiveType=\"char\"/>" +
            "    <type name=\"testTypeConstant\" presence=\"constant\" primitiveType=\"char\">A</type>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeDefault").getPresence(), is(Presence.REQUIRED));
        assertThat(map.get("testTypeRequired").getPresence(), is(Presence.REQUIRED));
        assertThat(map.get("testTypeOptional").getPresence(), is(Presence.OPTIONAL));
        assertThat(map.get("testTypeConstant").getPresence(), is(Presence.CONSTANT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownPresenceSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTyeUnknown\" presence=\"XXXXX\" primitiveType=\"char\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenNoPrimitiveTypeSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenNoNameSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type primitiveType=\"char\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldUseAppropriatePrimitiveType()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeChar\" primitiveType=\"char\"/>" +
            "    <type name=\"testTypeInt8\" primitiveType=\"int8\"/>" +
            "    <type name=\"testTypeInt16\" primitiveType=\"int16\"/>" +
            "    <type name=\"testTypeInt32\" primitiveType=\"int32\"/>" +
            "    <type name=\"testTypeInt64\" primitiveType=\"int64\"/>" +
            "    <type name=\"testTypeUInt8\" primitiveType=\"uint8\"/>" +
            "    <type name=\"testTypeUInt16\" primitiveType=\"uint16\"/>" +
            "    <type name=\"testTypeUInt32\" primitiveType=\"uint32\"/>" +
            "    <type name=\"testTypeUInt64\" primitiveType=\"uint64\"/>" +
            "    <type name=\"testTypeFloat\" primitiveType=\"float\"/>" +
            "    <type name=\"testTypeDouble\" primitiveType=\"double\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(((EncodedDataType)map.get("testTypeChar")).getPrimitiveType(), is(PrimitiveType.CHAR));
        assertThat(((EncodedDataType)map.get("testTypeInt8")).getPrimitiveType(), is(PrimitiveType.INT8));
        assertThat(((EncodedDataType)map.get("testTypeInt16")).getPrimitiveType(), is(PrimitiveType.INT16));
        assertThat(((EncodedDataType)map.get("testTypeInt32")).getPrimitiveType(), is(PrimitiveType.INT32));
        assertThat(((EncodedDataType)map.get("testTypeInt64")).getPrimitiveType(), is(PrimitiveType.INT64));
        assertThat(((EncodedDataType)map.get("testTypeUInt8")).getPrimitiveType(), is(PrimitiveType.UINT8));
        assertThat(((EncodedDataType)map.get("testTypeUInt16")).getPrimitiveType(), is(PrimitiveType.UINT16));
        assertThat(((EncodedDataType)map.get("testTypeUInt32")).getPrimitiveType(), is(PrimitiveType.UINT32));
        assertThat(((EncodedDataType)map.get("testTypeUInt64")).getPrimitiveType(), is(PrimitiveType.UINT64));
        assertThat(((EncodedDataType)map.get("testTypeFloat")).getPrimitiveType(), is(PrimitiveType.FLOAT));
        assertThat(((EncodedDataType)map.get("testTypeDouble")).getPrimitiveType(), is(PrimitiveType.DOUBLE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenUnknownPrimitiveTypeSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeUnknown\" primitiveType=\"XXXX\"/>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldReturnCorrectSizeForPrimitiveTypes()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeChar\" primitiveType=\"char\"/>" +
            "    <type name=\"testTypeInt8\" primitiveType=\"int8\"/>" +
            "    <type name=\"testTypeInt16\" primitiveType=\"int16\"/>" +
            "    <type name=\"testTypeInt32\" primitiveType=\"int32\"/>" +
            "    <type name=\"testTypeInt64\" primitiveType=\"int64\"/>" +
            "    <type name=\"testTypeUInt8\" primitiveType=\"uint8\"/>" +
            "    <type name=\"testTypeUInt16\" primitiveType=\"uint16\"/>" +
            "    <type name=\"testTypeUInt32\" primitiveType=\"uint32\"/>" +
            "    <type name=\"testTypeUInt64\" primitiveType=\"uint64\"/>" +
            "    <type name=\"testTypeFloat\" primitiveType=\"float\"/>" +
            "    <type name=\"testTypeDouble\" primitiveType=\"double\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(valueOf(map.get("testTypeChar").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeInt8").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeInt16").size()), is(valueOf(2)));
        assertThat(valueOf(map.get("testTypeInt32").size()), is(valueOf(4)));
        assertThat(valueOf(map.get("testTypeInt64").size()), is(valueOf(8)));
        assertThat(valueOf(map.get("testTypeUInt8").size()), is(valueOf(1)));
        assertThat(valueOf(map.get("testTypeUInt16").size()), is(valueOf(2)));
        assertThat(valueOf(map.get("testTypeUInt32").size()), is(valueOf(4)));
        assertThat(valueOf(map.get("testTypeUInt64").size()), is(valueOf(8)));
        assertThat(valueOf(map.get("testTypeFloat").size()), is(valueOf(4)));
        assertThat(valueOf(map.get("testTypeDouble").size()), is(valueOf(8)));
    }

    @Test
    public void shouldReturnCorrectDescriptionForType()
        throws Exception
    {
        final String desc = "basic description attribute of a type element";
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeDescription\" primitiveType=\"char\" description=\"" + desc + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testTypeDescription").getDescription(), is(desc));
    }

    @Test
    public void shouldReturnNullOnNoDescriptionSet()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeNoDescription\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        String description = map.get("testTypeNoDescription").getDescription();
        Assert.assertNull(description);
    }

    @Test
    public void shouldReturnCorrectSemanticTypeForType()
        throws Exception
    {
        final String semanticType = "char";
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType\" primitiveType=\"char\" semanticType=\"" + semanticType + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat(map.get("testType").getSemanticType(), is(semanticType));
    }

    @Test
    public void shouldReturnNullWhenSemanticTypeNotSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testType\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        Assert.assertNull(map.get("testType").getSemanticType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenConstantPresenceButNoDataSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypePresenceConst\" primitiveType=\"char\" presence=\"constant\"></type>" +
            "</types>";

        parseTestXmlWithMap("/types/type", testXmlString);
    }

    @Test
    public void shouldReturnCorrectPresenceConstantWhenSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypePresenceConst\" primitiveType=\"char\" presence=\"constant\">F</type>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypePresenceConst")).getConstValue()), is(parse("F", PrimitiveType.CHAR)));
    }

    @Test
    public void shouldReturnDefaultMinValueWhenSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeDefaultCharMinValue\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        Assert.assertNull(((EncodedDataType)map.get("testTypeDefaultCharMinValue")).getMinValue());
    }

    @Test
    public void shouldReturnDefaultMaxValueWhenSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeDefaultCharMaxValue\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        Assert.assertNull(((EncodedDataType)map.get("testTypeDefaultCharMaxValue")).getMaxValue());
    }

    @Test
    public void shouldReturnDefaultNullValueWhenSpecified()
        throws Exception
    {
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeDefaultCharNullValue\" primitiveType=\"char\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        Assert.assertNull(((EncodedDataType)map.get("testTypeDefaultCharNullValue")).getNullValue());
    }

    @Test
    public void shouldReturnCorrectMinValueWhenSpecified()
        throws Exception
    {
        final String minVal = "10";
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeInt8MinValue\" primitiveType=\"int8\" minVal=\"" + minVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8MinValue")).getMinValue()), is(parse(minVal, PrimitiveType.INT8)));
    }

    @Test
    public void shouldReturnCorrectMaxValueWhenSpecified()
        throws Exception
    {
        final String maxVal = "10";
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeInt8MaxValue\" primitiveType=\"int8\" maxVal=\"" + maxVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8MaxValue")).getMaxValue()), is(parse(maxVal, PrimitiveType.INT8)));
    }

    @Test
    public void shouldReturnCorrectNullValueWhenSpecified()
        throws Exception
    {
        final String nullVal = "10";
        final String testXmlString =
            "<types>" +
            "    <type name=\"testTypeInt8NullValue\" primitiveType=\"int8\" presence=\"optional\" nullVal=\"" + nullVal + "\"/>" +
            "</types>";

        Map<String, Type> map = parseTestXmlWithMap("/types/type", testXmlString);
        assertThat((((EncodedDataType)map.get("testTypeInt8NullValue")).getNullValue()),
                   is(parse(nullVal, PrimitiveType.INT8)));
    }

    private static Map<String, Type> parseTestXmlWithMap(final String xPathExpr, final String xml)
        throws ParserConfigurationException, XPathExpressionException, IOException, SAXException
    {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList list = (NodeList)xPath.compile(xPathExpr).evaluate(document, XPathConstants.NODESET);
        Map<String, Type> map = new HashMap<>();

        System.setProperty(SbeTool.SBE_VALIDATE_EXCEPTION, "true");
        System.setProperty(SbeTool.SBE_VALIDATE_OUTPUT_SUPPRESS, "true");
        document.setUserData(XmlSchemaParser.XML_ERROR_HANDLER_KEY, new ErrorHandler(), null);

        for (int i = 0, size = list.getLength(); i < size; i++)
        {
            Type t = new EncodedDataType(list.item(i));
            map.put(t.getName(), t);
        }

        return map;
    }
}
