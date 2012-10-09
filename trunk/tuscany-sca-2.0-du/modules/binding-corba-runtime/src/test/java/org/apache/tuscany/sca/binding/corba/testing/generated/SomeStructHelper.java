/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.tuscany.sca.binding.corba.testing.generated;

/**
* org/apache/tuscany/sca/binding/corba/testing/generated/SomeStructHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from general_tests.idl
* monday, 23 june 2008 2008 14:12:28 CEST
*/

abstract public class SomeStructHelper {
    private static String _id = "IDL:org/apache/tuscany/sca/binding/corba/testing/generated/SomeStruct/SomeStruct:1.0";

    public static void insert(org.omg.CORBA.Any a,
                              org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct extract(org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    private static boolean __active = false;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            synchronized (org.omg.CORBA.TypeCode.class) {
                if (__typeCode == null) {
                    if (__active) {
                        return org.omg.CORBA.ORB.init().create_recursive_tc(_id);
                    }
                    __active = true;
                    org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember[5];
                    org.omg.CORBA.TypeCode _tcOf_members0 = null;
                    _tcOf_members0 = org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.type();
                    _members0[0] = new org.omg.CORBA.StructMember("innerStruct", _tcOf_members0, null);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_string_tc(0);
                    _members0[1] = new org.omg.CORBA.StructMember("str", _tcOf_members0, null);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_string_tc(0);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.string_listHelper
                                                 .id(),
                                             "string_list",
                                             _tcOf_members0);
                    _members0[2] = new org.omg.CORBA.StructMember("str_list", _tcOf_members0, null);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Helper
                                                 .id(),
                                             "long_seq1",
                                             _tcOf_members0);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper
                                                 .id(),
                                             "long_seq2",
                                             _tcOf_members0);
                    _members0[3] = new org.omg.CORBA.StructMember("twoDimSeq", _tcOf_members0, null);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq1Helper
                                                 .id(),
                                             "long_seq1",
                                             _tcOf_members0);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper
                                                 .id(),
                                             "long_seq2",
                                             _tcOf_members0);
                    _tcOf_members0 = org.omg.CORBA.ORB.init().create_sequence_tc(0, _tcOf_members0);
                    _tcOf_members0 =
                        org.omg.CORBA.ORB.init()
                            .create_alias_tc(org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper
                                                 .id(),
                                             "long_seq3",
                                             _tcOf_members0);
                    _members0[4] = new org.omg.CORBA.StructMember("threeDimSeq", _tcOf_members0, null);
                    __typeCode =
                        org.omg.CORBA.ORB.init()
                            .create_struct_tc(org.apache.tuscany.sca.binding.corba.testing.generated.SomeStructHelper
                                                  .id(),
                                              "SomeStruct",
                                              _members0);
                    __active = false;
                }
            }
        }
        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct read(org.omg.CORBA.portable.InputStream istream) {
        org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct value =
            new org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct();
        value.innerStruct = org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.read(istream);
        value.str = istream.read_string();
        value.str_list = org.apache.tuscany.sca.binding.corba.testing.generated.string_listHelper.read(istream);
        value.twoDimSeq = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper.read(istream);
        value.threeDimSeq = org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper.read(istream);
        return value;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream,
                             org.apache.tuscany.sca.binding.corba.testing.generated.SomeStruct value) {
        org.apache.tuscany.sca.binding.corba.testing.generated.SimpleStructHelper.write(ostream, value.innerStruct);
        ostream.write_string(value.str);
        org.apache.tuscany.sca.binding.corba.testing.generated.string_listHelper.write(ostream, value.str_list);
        org.apache.tuscany.sca.binding.corba.testing.generated.long_seq2Helper.write(ostream, value.twoDimSeq);
        org.apache.tuscany.sca.binding.corba.testing.generated.long_seq3Helper.write(ostream, value.threeDimSeq);
    }

}
