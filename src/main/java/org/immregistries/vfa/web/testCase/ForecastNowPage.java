/*
 * Copyright 2013 - Texas Children's Hospital
 * 
 *   Texas Children's Hospital licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.immregistries.vfa.web.testCase;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.Session;
import org.immregistries.vfa.connect.model.Software;
import org.immregistries.vfa.manager.ForecastActualGenerator;
import org.immregistries.vfa.model.TestPanelCase;
import org.immregistries.vfa.model.User;
import org.immregistries.vfa.web.FTBasePage;
import org.immregistries.vfa.web.MenuSection;
import org.immregistries.vfa.web.SecurePage;
import org.immregistries.vfa.web.WebSession;
import org.immregistries.vfa.web.software.SelectSoftwarePage;

public class ForecastNowPage extends FTBasePage implements SecurePage {
  private static final long serialVersionUID = 1L;

  private ArrayList<Software> softwareSelected = new ArrayList<Software>();

  public ForecastNowPage(final PageParameters pageParameters) {
    super(MenuSection.TEST_CASE, pageParameters);
    WebSession webSession = ((WebSession) getSession());
    final User user = webSession.getUser();
    final TestPanelCase testPanelCase = user.getSelectedTestPanelCase();


    final Session dataSession = webSession.getDataSession();

    final List<Software> softwareList = SelectSoftwarePage.getSoftwareList(webSession);

    final CheckBox[] checkBoxes = new CheckBox[softwareList.size()];
    for (int i = 0; i < checkBoxes.length; i++) {
      checkBoxes[i] = new CheckBox("checkbox" + i, Model.of(Boolean.TRUE));
    }

    final CheckBoxMultipleChoice<Software> softwareCheckBox = new CheckBoxMultipleChoice<Software>("softwares",
        new Model(softwareSelected), softwareList);

    Form<?> forecastNowForm = new Form<Void>("forecastNowForm") {
      @Override
      protected void onSubmit() {
        for (Software software : softwareSelected) {
          try {
            ForecastActualGenerator.runForecastActual(testPanelCase, software, dataSession, true);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        setResponsePage(new ActualVsExpectedPage());

      }
    };
    forecastNowForm.add(softwareCheckBox);
    add(forecastNowForm);

  }

}
