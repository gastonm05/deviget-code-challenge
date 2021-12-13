package api;

import helpers.api.ApiHelper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NasaTest {
	private final String apiUri = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";
	private final String api_key = "hYP0336a4td1U4dvEMkLHzAAf3FcNKwLdwCocabK";
	private final HashMap<String, Object> queryParams = new HashMap<>();

	@BeforeMethod()
	public void beforeMethod() {
		queryParams.clear();
	}

	/***
	 * Test 1: Retrieve the first 10 Mars photos made by "Curiosity" on 1000 Martian
	 * sol.
	 */

	@Test(priority = 1)
	public void retrievePhotosBySolDate() {

		// Setup Request Headers
		queryParams.put("sol", "1000");
		queryParams.put("api_key", api_key);
		queryParams.put("page", "1");

		// Execute Request
		Response response = ApiHelper.get(apiUri, queryParams, "");

		// ASSERT
		response.then().assertThat().statusCode(200);

		// Create List of results
		List<String> photos = response.jsonPath().getList("photos");
		photos.subList(10, photos.size()).clear();

		Assert.assertTrue(photos.size() == 10);

	}

	/***
	 * Test 2: Retrieve the first 10 Mars photos made by "Curiosity" on Earth date
	 * equal to 1000 Martian sol.
	 */

	@Test(priority = 2)
	public void retrievePhotosByEarthDate() {

		// Setup Request Headers
		queryParams.put("earth_date", "2015-05-30");
		queryParams.put("api_key", api_key);
		queryParams.put("page", "1");

		// Execute Request
		Response response = ApiHelper.get(apiUri, queryParams, "");

		// Create List of results
		List<String> photos = response.jsonPath().getList("photos");
		photos.subList(10, photos.size()).clear();

		Assert.assertTrue(photos.size() == 10);
	}

	/***
	 * Test 3: Retrieve and compare the first 10 Mars photos made by "Curiosity" on
	 * 1000 sol and on Earth date equal to 1000 Martian sol.
	 */

	@Test(priority = 3)
	public void retrieveAndCompareTwoPhotoSets() {

		// Setup Request Headers
		queryParams.put("sol", "1000");
		queryParams.put("api_key", api_key);
		queryParams.put("page", "1");

		// Execute Request
		Response response1 = ApiHelper.get(apiUri, queryParams, "");

		// Setup Request Headers
		queryParams.clear();
		queryParams.put("earth_date", "2015-05-30");
		queryParams.put("api_key", api_key);
		queryParams.put("page", "1");

		// Execute Request
		Response response2 = ApiHelper.get(apiUri, queryParams, "");

		// Create Lists of results
		List<String> photos1 = response1.jsonPath().getList("photos");
		photos1.subList(10, photos1.size()).clear();

		List<String> photos2 = response2.jsonPath().getList("photos");
		photos2.subList(10, photos2.size()).clear();

		// Compare lists to verify that any element of the second page is in first page.
		Assert.assertTrue(photos1.containsAll(photos2));
		Assert.assertTrue(photos2.containsAll(photos1));

	}

	/***
	 * Test 4: Validate that the amounts of pictures that each "Curiosity" camera
	 * took on 1000 Mars sol is not greater than 10 times the amount taken by other
	 * cameras on the same date.
	 */
	@Test(priority = 4)
	public void validateCamerasPhotoAmounts() {

		// Setup Request Headers
		queryParams.put("api_key", api_key);
		queryParams.put("sol", "1000");

		// Execute Request
		Response response = ApiHelper.get(apiUri, queryParams, "");

		// Get a List of the Cameras that took photos that day
		List<String> listCameras = response.jsonPath().get("photos.camera.name");
		List<String> cameras = new ArrayList<>(new HashSet<>(listCameras));

		// Get the total number of photos taken
		List<String> allPhotos = response.jsonPath().getList("photos");
		int totalPhotos = allPhotos.size();

		// Compare number of photos taken by each camera
		for (String camera : cameras) {

			queryParams.put("camera", camera);
			Response response2 = ApiHelper.get(apiUri, queryParams, "");

			List<String> photos = response2.jsonPath().getList("photos");
			int photosCamera = photos.size();

			Assert.assertTrue(photosCamera < (totalPhotos - photosCamera) * 10);
		}

	}
}
