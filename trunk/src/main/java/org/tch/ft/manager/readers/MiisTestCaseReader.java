package org.tch.ft.manager.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.fc.model.Event;
import org.tch.fc.model.VaccineGroup;
import org.tch.fc.model.TestCase;
import org.tch.fc.model.TestEvent;
import org.tch.ft.model.ForecastExpected;
import org.tch.ft.model.TestCaseWithExpectations;

public class MiisTestCaseReader extends CsvTestCaseReader implements TestCaseReader {

  private static final String FIELD_CASE = "Case";
  private static final String FIELD_IZ_SERIES = "IZ Series";
  private static final String FIELD_BIRTHDATE = "birthdate";
  private static final String FIELD_GENDER = "Gender";
  private static final String FIELD_VACCINE_NAMES = "Dosage History";
  private static final String FIELD_SHOT_ = "Shot";
  private static final String FIELD_SHOT_CVX = " CVX";
  private static final String FIELD_SHOT_DATE = " Date";
  private static final String FIELD_FORECAST_NUM = "Forecast#";
  private static final String FIELD_EARLIEST_DATE = "Earliest Date";
  private static final String FIELD_REC_DATE = "Rec Date";
  private static final String FIELD_OVERDUE_DATE = "Overdue Date";
  private static final String FIELD_REFERENCE_DATE = "Reference Date";

  private Map<String, VaccineGroup> vaccineGroupMap = new HashMap<String, VaccineGroup>();

  public void setVaccineGroupss(Map<Integer, VaccineGroup> vaccineGroupListMap) {
    vaccineGroupMap.put("DTP", vaccineGroupListMap.get(2));
    vaccineGroupMap.put("DTaP", vaccineGroupListMap.get(2));
    vaccineGroupMap.put("H1N1", vaccineGroupListMap.get(3));
    vaccineGroupMap.put("HepA", vaccineGroupListMap.get(4));
    vaccineGroupMap.put("HepB", vaccineGroupListMap.get(5));
    vaccineGroupMap.put("HerpesZoster", vaccineGroupListMap.get(14));
    vaccineGroupMap.put("Hib", vaccineGroupListMap.get(6));
    vaccineGroupMap.put("HPV", vaccineGroupListMap.get(7));
    vaccineGroupMap.put("Influenza", vaccineGroupListMap.get(3));
    vaccineGroupMap.put("Flu", vaccineGroupListMap.get(3));
    vaccineGroupMap.put("MCV4", vaccineGroupListMap.get(8));
    vaccineGroupMap.put("MCV", vaccineGroupListMap.get(8));
    vaccineGroupMap.put("MMR", vaccineGroupListMap.get(9));
    vaccineGroupMap.put("MPSV", vaccineGroupListMap.get(8));
    vaccineGroupMap.put("Pneumo-Poly", vaccineGroupListMap.get(10));
    vaccineGroupMap.put("Pneumonia", vaccineGroupListMap.get(10));
    vaccineGroupMap.put("PCV", vaccineGroupListMap.get(10));
    vaccineGroupMap.put("Polio", vaccineGroupListMap.get(11));
    vaccineGroupMap.put("POL", vaccineGroupListMap.get(11));
    vaccineGroupMap.put("Rotavirus", vaccineGroupListMap.get(12));
    vaccineGroupMap.put("Rota", vaccineGroupListMap.get(12));
    vaccineGroupMap.put("Td/Tdap", vaccineGroupListMap.get(15));
    vaccineGroupMap.put("Varicella", vaccineGroupListMap.get(13));
    vaccineGroupMap.put("Var", vaccineGroupListMap.get(13));
    vaccineGroupMap.put("Typhoid", vaccineGroupListMap.get(0));
  }

  private String[] ignoredItems = { "Typhoid" };

  public void read(InputStream in) throws IOException {
    readInputStream(in);
    if (testCaseFieldListList.size() <= 1) {
      throw new IllegalArgumentException("No test cases found");
    }
    headerFields = testCaseFieldListList.get(0);
    testCaseFieldListList.remove(0);

    int caseNumberPosition = findFieldPos(FIELD_CASE);
    int izSeriesPos = findFieldPos(FIELD_IZ_SERIES);
    int birthdatePos = findFieldPos(FIELD_BIRTHDATE);
    int genderPos = findFieldPos(FIELD_GENDER);
    int vaccineNamesPos = findFieldPos(FIELD_VACCINE_NAMES);
    int shotCvxPos[] = new int[10];
    int shotDatePos[] = new int[10];
    for (int i = 1; i <= 9; i++) {
      shotCvxPos[i] = findFieldPos(FIELD_SHOT_ + i + FIELD_SHOT_CVX);
      shotDatePos[i] = findFieldPos(FIELD_SHOT_ + i + FIELD_SHOT_DATE);
    }
    int forecastNumPos = findFieldPos(FIELD_FORECAST_NUM);
    int earliestDatePos = findFieldPos(FIELD_EARLIEST_DATE);
    int recDatePos = findFieldPos(FIELD_REC_DATE);
    int overdueDatePos = findFieldPos(FIELD_OVERDUE_DATE);
    int referenceDatePos = findFieldPos(FIELD_REFERENCE_DATE);

    Date referenceDate = null;
    for (List<String> testCaseFieldList : testCaseFieldListList) {
      TestCaseWithExpectations testCaseWithExpectations = new TestCaseWithExpectations();
      TestCase testCase = testCaseWithExpectations.getTestCase();
      testCaseList.add(testCaseWithExpectations);
      testCase.setTestCaseNumber(readField(caseNumberPosition, testCaseFieldList));
      testCase.setCategoryName(readField(izSeriesPos, testCaseFieldList));
      testCase.setLabel("Test Case " + testCase.getTestCaseNumber());
      testCase.setDescription(readField(vaccineNamesPos, testCaseFieldList));
      testCase.setPatientDob(readDateField(birthdatePos, testCaseFieldList, testCaseWithExpectations));
      testCase.setPatientSex(readField(genderPos, testCaseFieldList).toUpperCase().startsWith("M") ? "M" : "F");
      if (referenceDate == null) {
        referenceDate = readDateField(referenceDatePos, testCaseFieldList, testCaseWithExpectations);
      }
      testCase.setEvalDate(referenceDate);
      List<TestEvent> testEventList = new ArrayList<TestEvent>();
      testCase.setTestEventList(testEventList);
      for (int i = 1; i <= 9; i++) {
        String cvxCode = readField(shotCvxPos[i], testCaseFieldList);
        if (cvxCode.length() == 1) {
          cvxCode = "0" + cvxCode;
        }

        Date shotDate = readDateField(shotDatePos[i], testCaseFieldList, testCaseWithExpectations);
        if (!cvxCode.equals("") && shotDate != null) {
          TestEvent testEvent = new TestEvent();
          Event event = cvxToEventMap.get(cvxCode);
          if (event == null) {
            throw new IllegalArgumentException("Unrecognized CVX code '" + cvxCode + "' for Shot" + i + " test case "
                + testCase.getTestCaseNumber() + "");
          }
          testEvent.setEvent(event);
          testEvent.setEventDate(shotDate);
          testEvent.setTestCase(testCase);
          testEventList.add(testEvent);
        }
      }
      VaccineGroup vaccineGroupItem = vaccineGroupMap.get(testCase.getCategoryName());
      if (vaccineGroupItem == null) {
        boolean found = false;
        for (String ignoredItem : ignoredItems) {
          if (testCase.getCategoryName().equals(ignoredItem)) {
            found = true;
            continue;
          }
        }
        if (!found) {
          throw new IllegalArgumentException("Unrecognized category name '" + testCase.getCategoryName() + "'");
        }
      } else {
        ForecastExpected forecastExpected = new ForecastExpected();
        forecastExpected.setTestCase(testCase);
        forecastExpected.setAuthor(user);
        forecastExpected.setVaccineGroup(vaccineGroupItem);
        forecastExpected.setDoseNumber(readField(forecastNumPos, testCaseFieldList));
        forecastExpected.setValidDate(readDateField(earliestDatePos, testCaseFieldList, testCaseWithExpectations));
        forecastExpected.setDueDate(readDateField(recDatePos, testCaseFieldList, testCaseWithExpectations));
        forecastExpected.setOverdueDate(readDateField(overdueDatePos, testCaseFieldList, testCaseWithExpectations));
        List<ForecastExpected> forecastExpectedList = testCaseWithExpectations.getForecastExpectedList();
        if (forecastExpectedList == null) {
          forecastExpectedList = new ArrayList<ForecastExpected>();
          testCaseWithExpectations.setForecastExpectedList(forecastExpectedList);
        }
        forecastExpectedList.add(forecastExpected);

      }
    }
  }

}