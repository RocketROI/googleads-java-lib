// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package adwords.axis.v201607.optimization;

import com.google.api.ads.adwords.axis.factory.AdWordsServices;
import com.google.api.ads.adwords.axis.v201607.cm.Criterion;
import com.google.api.ads.adwords.axis.v201607.cm.Keyword;
import com.google.api.ads.adwords.axis.v201607.cm.KeywordMatchType;
import com.google.api.ads.adwords.axis.v201607.cm.Language;
import com.google.api.ads.adwords.axis.v201607.cm.Location;
import com.google.api.ads.adwords.axis.v201607.cm.Money;
import com.google.api.ads.adwords.axis.v201607.o.AdGroupEstimateRequest;
import com.google.api.ads.adwords.axis.v201607.o.CampaignEstimate;
import com.google.api.ads.adwords.axis.v201607.o.CampaignEstimateRequest;
import com.google.api.ads.adwords.axis.v201607.o.KeywordEstimate;
import com.google.api.ads.adwords.axis.v201607.o.KeywordEstimateRequest;
import com.google.api.ads.adwords.axis.v201607.o.PlatformCampaignEstimate;
import com.google.api.ads.adwords.axis.v201607.o.StatsEstimate;
import com.google.api.ads.adwords.axis.v201607.o.TrafficEstimatorResult;
import com.google.api.ads.adwords.axis.v201607.o.TrafficEstimatorSelector;
import com.google.api.ads.adwords.axis.v201607.o.TrafficEstimatorServiceInterface;
import com.google.api.ads.adwords.lib.client.AdWordsSession;
import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.client.auth.oauth2.Credential;

import java.util.ArrayList;
import java.util.List;

/**
 * This example gets keyword traffic estimates.
 *
 * <p>Credentials and properties in {@code fromFile()} are pulled from the
 * "ads.properties" file. See README for more info.
 */
public class EstimateKeywordTraffic {

  public static void main(String[] args) throws Exception {
    // Generate a refreshable OAuth2 credential.
    Credential oAuth2Credential = new OfflineCredentials.Builder()
        .forApi(Api.ADWORDS)
        .fromFile()
        .build()
        .generateCredential();

    // Construct an AdWordsSession.
    AdWordsSession session = new AdWordsSession.Builder()
        .fromFile()
        .withOAuth2Credential(oAuth2Credential)
        .build();

    AdWordsServices adWordsServices = new AdWordsServices();

    runExample(adWordsServices, session);
  }

  public static void runExample(
      AdWordsServices adWordsServices, AdWordsSession session) throws Exception {
    // Get the TrafficEstimatorService.
    TrafficEstimatorServiceInterface trafficEstimatorService =
        adWordsServices.get(session, TrafficEstimatorServiceInterface.class);

    // Create keywords. Refer to the TrafficEstimatorService documentation for the maximum
    // number of keywords that can be passed in a single request.
    //   https://developers.google.com/adwords/api/docs/reference/latest/TrafficEstimatorService
    List<Keyword> keywords = new ArrayList<Keyword>();

    Keyword marsCruiseKeyword = new Keyword();
    marsCruiseKeyword.setText("mars cruise");
    marsCruiseKeyword.setMatchType(KeywordMatchType.BROAD);
    keywords.add(marsCruiseKeyword);

    Keyword cheapCruiseKeyword = new Keyword();
    cheapCruiseKeyword.setText("cheap cruise");
    cheapCruiseKeyword.setMatchType(KeywordMatchType.PHRASE);
    keywords.add(cheapCruiseKeyword);

    Keyword cruiseKeyword = new Keyword();
    cruiseKeyword.setText("cruise");
    cruiseKeyword.setMatchType(KeywordMatchType.EXACT);
    keywords.add(cruiseKeyword);

    // Create a keyword estimate request for each keyword.
    List<KeywordEstimateRequest> keywordEstimateRequests = new ArrayList<KeywordEstimateRequest>();
    for (Keyword keyword : keywords) {
      KeywordEstimateRequest keywordEstimateRequest = new KeywordEstimateRequest();
      keywordEstimateRequest.setKeyword(keyword);
      keywordEstimateRequests.add(keywordEstimateRequest);
    }

    // Add a negative keyword to the traffic estimate.
    KeywordEstimateRequest negativeKeywordEstimateRequest = new KeywordEstimateRequest();
    negativeKeywordEstimateRequest.setKeyword(new Keyword(null, null, null, "hiking tour",
        KeywordMatchType.BROAD));
    negativeKeywordEstimateRequest.setIsNegative(true);
    keywordEstimateRequests.add(negativeKeywordEstimateRequest);

    // Create ad group estimate requests.
    List<AdGroupEstimateRequest> adGroupEstimateRequests = new ArrayList<AdGroupEstimateRequest>();
    AdGroupEstimateRequest adGroupEstimateRequest = new AdGroupEstimateRequest();
    adGroupEstimateRequest.setKeywordEstimateRequests(keywordEstimateRequests
        .toArray(new KeywordEstimateRequest[] {}));
    adGroupEstimateRequest.setMaxCpc(new Money(null, 1000000L));
    adGroupEstimateRequests.add(adGroupEstimateRequest);

    // Create campaign estimate requests.
    List<CampaignEstimateRequest> campaignEstimateRequests =
        new ArrayList<CampaignEstimateRequest>();
    CampaignEstimateRequest campaignEstimateRequest = new CampaignEstimateRequest();
    campaignEstimateRequest.setAdGroupEstimateRequests(adGroupEstimateRequests
        .toArray(new AdGroupEstimateRequest[] {}));

    Location unitedStates = new Location();
    unitedStates.setId(2840L);
    Language english = new Language();
    english.setId(1000L);
    campaignEstimateRequest.setCriteria(new Criterion[] {unitedStates, english});
    campaignEstimateRequests.add(campaignEstimateRequest);

    // Create selector.
    TrafficEstimatorSelector selector = new TrafficEstimatorSelector();
    selector.setCampaignEstimateRequests(campaignEstimateRequests
        .toArray(new CampaignEstimateRequest[] {}));

    // Optional: Request a list of campaign level estimates segmented by platform.
    selector.setPlatformEstimateRequested(true);

    // Get traffic estimates.
    TrafficEstimatorResult result = trafficEstimatorService.get(selector);

    // Display traffic estimates.
    if (result != null
        && result.getCampaignEstimates() != null
        && result.getCampaignEstimates().length > 0) {
      CampaignEstimate campaignEstimate = result.getCampaignEstimates()[0];

      // Display the campaign level estimates segmented by platform.
      if (campaignEstimate.getPlatformEstimates() != null) {
        for (PlatformCampaignEstimate platformEstimate : campaignEstimate.getPlatformEstimates()) {
          String platformMessage =
              String.format(
                  "Results for the platform with ID %d and name '%s':%n",
                  platformEstimate.getPlatform().getId(),
                  platformEstimate.getPlatform().getPlatformName());
          displayMeanEstimates(
              platformMessage,
              platformEstimate.getMinEstimate(),
              platformEstimate.getMaxEstimate());
        }
      }

      // Display the keyword estimates.
      KeywordEstimate[] keywordEstimates =
          campaignEstimate.getAdGroupEstimates()[0].getKeywordEstimates();
      for (int i = 0; i < keywordEstimates.length; i++) {
        if (Boolean.TRUE.equals(keywordEstimateRequests.get(i).getIsNegative())) {
          continue;
        }

        Keyword keyword = keywordEstimateRequests.get(i).getKeyword();
        KeywordEstimate keywordEstimate = keywordEstimates[i];

        String keywordMessage =
            String.format(
                "Results for the keyword with text '%s' and match type '%s':%n",
                keyword.getText(), keyword.getMatchType());
        displayMeanEstimates(keywordMessage, keywordEstimate.getMin(), keywordEstimate.getMax());
      }
    } else {
      System.out.println("No traffic estimates were returned.");
    }
  }

  /**
   * Writes the mean estimates for the given min and max estimates to {@code System.out},
   * prefixed with {@code message}.
   */
  private static void displayMeanEstimates(String message, StatsEstimate minEstimate,
      StatsEstimate maxEstimate) {
    // Find the mean of the min and max values.
    Double meanAverageCpc = calculateMean(minEstimate.getAverageCpc(),
        maxEstimate.getAverageCpc());
    Double meanAveragePosition = calculateMean(minEstimate.getAveragePosition(),
        maxEstimate.getAveragePosition());
    Double meanClicks = calculateMean(minEstimate.getClicksPerDay(),
        maxEstimate.getClicksPerDay());
    Double meanTotalCost = calculateMean(minEstimate.getTotalCost(),
        maxEstimate.getTotalCost());

    System.out.println(message);
    System.out.printf("\tEstimated average CPC: %s%n", formatMean(meanAverageCpc));
    System.out.printf("\tEstimated ad position: %s%n", formatMean(meanAveragePosition));
    System.out.printf("\tEstimated daily clicks: %s%n", formatMean(meanClicks));
    System.out.printf("\tEstimated daily cost: %s%n%n", formatMean(meanTotalCost));
  }

  /**
   * Returns a formatted version of {@code mean}, handling nulls.
   */
  private static String formatMean(Double mean) {
    if (mean == null) {
      // Handle nulls separately, else the %.2f format below will
      // truncate 'null' to 'nu'.
      return null;
    }
    return String.format("%.2f", mean);
  }

  /**
   * Returns the mean of the {@code microAmount} of the two Money values if neither is null, else
   * returns null.
   */
  private static Double calculateMean(Money minMoney, Money maxMoney) {
    if (minMoney == null || maxMoney == null) {
      return null;
    }
    return calculateMean(minMoney.getMicroAmount(), maxMoney.getMicroAmount());
  }

  /**
   * Returns the mean of the two Number values if neither is null, else returns null.
   */
  private static Double calculateMean(Number min, Number max) {
    if (min == null || max == null) {
      return null;
    }
    return (min.doubleValue() + max.doubleValue()) / 2;
  }
}
