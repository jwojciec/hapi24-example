package parser;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.*;
import ca.uhn.hl7v2.model.v24.message.ADT_A05;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;

public class ParserHL7 {
    public static void startSimpleExample() {
        String msg = "MSH|^~\\&|IPM|LSP|RAD|RGQ|20100705100137||ADT^A28|765043596|P|2.4|12478673\r" +
                "EVN|A28|20100705100131\r" +
                "PID|||111111^^^RGQ^MR~2222222222^^^NHS^NH||Kowalski^Jan^Maria^III^Mr||20110105000000|Male|||RandomStreet^128B^RandomCity^RandomState^3333||4444444444|5555555555||M|||||||||||||20160709224441|Y\r" +
                "PD1|||PracticeName^^PracticeCode|GPCode^GPSurname^GPForename^^^DR^^NATGP\r";

        HapiContext context = new DefaultHapiContext();
        context.getParserConfiguration().setValidating(false);
        //Parser p = context.getGenericParser();
        Parser p = context.getPipeParser();

        Message hapiMsg;
        try {
            hapiMsg = p.parse(msg);
        } catch (EncodingNotSupportedException e) {
            e.printStackTrace();
            return;
        } catch (HL7Exception e) {
            e.printStackTrace();
            return;
        }

        /*
            ADT_A28 messages use objects of class ADT_A05 because that's their message structure
            source: https://github.com/crs4/hl7apy/issues/16
         */
        ADT_A05 adtMsg = (ADT_A05) hapiMsg;
        ca.uhn.hl7v2.model.v24.segment.MSH msh = adtMsg.getMSH();

        // Prints "ADT A28"
        String msgType = msh.getMessageType().toString();
        String msgTrigger = msh.getMessageType().getTriggerEvent().getValue();
        System.out.println(msgType + " " + msgTrigger);
        System.out.println("\n*** PATIENT MAPPING ***");

        /*
            Patient Mapping based on:
            http://hl7api.sourceforge.net/v24/apidocs/ca/uhn/hl7v2/model/v24/segment/PID.html
            https://www.hl7.org/fhir/patient-mappings.html#v2
         */

        //identifier	PID-3
        CX[] identifier = adtMsg.getPID().getPid3_PatientIdentifierList();
        System.out.println("identifiers:");
        for (CX cx : identifier) {
            System.out.println("\tidentifier: " + cx);
            System.out.println("\tidentifier tokens: " + cx.getID() + " "
                    + cx.getAssigningAuthority().getNamespaceID() + " "
                    + cx.getAssigningAuthority().getNamespaceID() + " "
                    + cx.getIdentifierTypeCode());
        }

        //active ??
        //name	PID-5, PID-9
        XPN[] patientNames = adtMsg.getPID().getPatientName();
        System.out.println("patient names:");
        for (XPN patientName : patientNames) {
            FN familyName = patientName.getFamilyName();
            ST name = patientName.getGivenName();
            ST furtherNames = patientName.getSecondAndFurtherGivenNamesOrInitialsThereof();
            ST prefix = patientName.getPrefixEgDR();
            ST suffix = patientName.getSuffixEgJRorIII();
            System.out.println("\tfamily name: " + familyName.getSurname());
            System.out.println("\tgiven name: " + name);
            System.out.println("\tfurher names: " + furtherNames);
            System.out.println("\tprefix: " + prefix);
            System.out.println("\tsuffix: " + suffix);
        }

        //telecom PID-13, PID-14, PID-40
        XTN[] phoneHome = adtMsg.getPID().getPhoneNumberHome();
        System.out.println("phone Home: ");
        for (XTN phone : phoneHome) {
            System.out.println("\tphone: " + phone.get9999999X99999CAnyText());
        }

        XTN[] phoneBusiness = adtMsg.getPID().getPhoneNumberBusiness();
        System.out.println("phone Business: ");
        for (XTN phone : phoneBusiness) {
            System.out.println("\tphone: " + phone.get9999999X99999CAnyText());
        }

        //gender	PID-8
        IS gender = adtMsg.getPID().getAdministrativeSex();
        System.out.println("gender: " + gender.getValue());

        //birthDate	PID-7
        TS dob = adtMsg.getPID().getDateTimeOfBirth();
        System.out.println("birth date: " + dob.getTimeOfAnEvent());

        //deceased[x]	PID-30 (bool) and PID-29 (datetime)
        TS deceasedDate = adtMsg.getPID().getPatientDeathDateAndTime();
        ID deceasedInd = adtMsg.getPID().getPatientDeathIndicator();
        System.out.println("is dead: " + deceasedInd);
        System.out.println("death date: " + deceasedDate.getTimeOfAnEvent());

        //address	PID-11
        XAD[] addresses = adtMsg.getPID().getPatientAddress();
        System.out.println("addresses: ");
        for (XAD address : addresses) {
            System.out.println("\taddress: " + address);
            System.out.println("\taddress tokens: " + address.getStreetAddress().getStreetOrMailingAddress() + " "
                    + address.getOtherDesignation() + " "
                    + address.getCity() + " "
                    + address.getStateOrProvince() + " "
                    + address.getZipOrPostalCode());
        }

        //maritalStatus	PID-16
        CE martialStatus = adtMsg.getPID().getMaritalStatus();
        System.out.println("marital Status: " + martialStatus.getIdentifier());

        //ignore others?
        //multipleBirth[x]	PID-24 (bool), PID-25 (integer)
        //photo	OBX-5 - needs a profile
        //contact
        //relationship	NK1-7, NK1-3
        //name	NK1-2
        //telecom	NK1-5, NK1-6, NK1-40
        //address	NK1-4
        //gender	NK1-15
        //organization	NK1-13, NK1-30, NK1-31, NK1-32, NK1-41
        //period
        //        animal
        //species	PID-35
        //breed	PID-37
        //genderStatus	N/A
        //communication
        //language	PID-15, LAN-2
        //preferred	PID-15
        //careProvider	PD1-4
        //managingOrganization
        //link
        //other	PID-3, MRG-1
        //type
    }
}