package healthrater;

import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HealthRaterApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("🍎 HealthRater");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 255, 250));

        JLabel titleLabel = new JLabel("🥗 HealthRater", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(34, 139, 34));
        frame.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(245, 255, 250));
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JTextField inputField = new JTextField(20);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JButton analyzeButton = new JButton("🔍 Analyze");
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        analyzeButton.setBackground(new Color(173, 216, 230));

        // 👇 Hover effect
        analyzeButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                analyzeButton.setBackground(Color.DARK_GRAY);
                analyzeButton.setForeground(Color.WHITE);
            }

            public void mouseExited(MouseEvent e) {
                analyzeButton.setBackground(new Color(173, 216, 230));
                analyzeButton.setForeground(Color.BLACK);
            }
        });

        // 👇 Analyze logic
        analyzeButton.addActionListener(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter some ingredients or a product name.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int score = getHealthScore(input);
            String emoji = score <= 3 ? "🔴" : score <= 6 ? "🟡" : "🟢";
            String usdaData = fetchUSDAData(input); // 📡 USDA API call

            // Parse the USDA data and display it in the app
            String displayData = parseUSDAData(usdaData);
            String productRating = rateProductBasedOnNutrition(usdaData); // Rate product based on nutrients

            // Provide an alternative suggestion
            String alternative = getHealthierAlternative(input);

            String message = "<html><h2>Health Score: " + emoji + " " + score + "/10</h2>" +
                    "<p>Detected: <b>" + input + "</b></p>" +
                    "<p><i>USDA data fetched (see below)</i></p><br>" + displayData +
                    "<br><b>Product Rating:</b> " + productRating + "<br><b>Alternative Suggestion:</b> " + alternative + "</html>";

            JLabel resultLabel = new JLabel(message);
            resultLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            JOptionPane.showMessageDialog(frame, resultLabel, "Analysis Result", JOptionPane.INFORMATION_MESSAGE);
        });

        inputPanel.add(inputField);
        inputPanel.add(analyzeButton);
        frame.add(inputPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // 💡 Health scoring logic
    public static int getHealthScore(String input) {
        input = input.toLowerCase();

        // Unhealthy Ingredients
        String[] unhealthyIngredients = {
            "sugar", "corn syrup", "msg", "maida", "preservative", "sodium benzoate", "aspartame", "acesulfame", 
            "hydrogenated", "palm oil", "maltodextrin", "artificial color", "artificial flavor", "refined flour", 
            "trans fat", "dye", "sodium nitrate", "glutamate", "butter", "cheese", "condensed milk", "ice cream", 
            "carbonated", "sweetener", "deep fried", "vegetable oil", "palm kernel oil", "hydrogenated fat", "caffeine",
            // Add more items here...
        };

        // Moderate Ingredients
        String[] moderateIngredients = {
            "salt", "edible oil", "baking soda", "citric acid", "starch", "vinegar", "yeast", "rice", "wheat", "potato", 
            "sunflower oil", "mustard oil", "sesame oil", "peanut oil", "ghee", "milk", "paneer", "cream", "tomato", 
            "green chili", "ginger", "garlic", "turmeric", "cumin", "coriander", "cinnamon", "cardamom", "clove", 
            // Add more items here...
        };

        // Healthy Ingredients
        String[] healthyIngredients = {
            "oats", "millets", "ragi", "quinoa", "brown rice", "almond", "flax", "chia", "turmeric", "spinach", "carrot", 
            "broccoli","Paneer", "cauliflower", "cucumber", "avocado", "sweet potato", "apple", "banana", "grapes", "blueberries", 
            "kiwi", "papaya", "guava", "dragonfruit", "goji berries", "mushroom", "kale", "lettuce", "mint", "parsley", 
            "basil", "green tea", "pistachio", "walnut", "flax seeds", "chia seeds", "sunflower seeds", "dates", "fig", 
            "squash", "zucchini", "apricot", "pomegranate", "bitter gourd", "amaranth", 
            // Add more items here...
        };

        // Unhealthy Brands
        String[] unhealthyBrands = {
            "oreo", "maggi", "lays", "kurkure", "pepsi", "coca cola", "fanta", "7up", "bisleri", "frooti", "thums up",
            "red bull", "kit kat", "mars", "snickers", "twix", "toblerone", "maggie noodles", "instant noodles", "cheetos",
            "pringles", "munch", "yippee", "top ramen", "monster energy", "choco pie", "gatorade", "doritos", "soda",
            "kellogg's cornflakes", "jack daniels", "vodka", "beer", "whisky", "bacardi", "johnnie walker",
            // Add more brands here...
        };

        // Moderate Brands
        String[] moderateBrands = {
            "amul", "mother dairy", "britannia", "parle", "nestle", "patanjali", "dove", "sunsilk", "tata tea", 
            "red label", "dabur", "patanjali", "tata salt", "indulekha", "loreal", "saffola", "maggi", "salt", 
            // Add more brands here...

        };

        // Healthy Brands
        String[] healthyBrands = {
            "organic india", "urban platter", "raw pressery", "juicy chemistry", "gits", "sattvik", "toffe", "happilo",
            "california almonds", "simply wholesome", "barkat", "nature's path", "soulfull", "tata organic", "amys kitchen", 
            "saffola", "organo gold", "earth mama", "biogreen", "sundrop", "zoe", 
            // Add more brands here...
        };

        // Unhealthy Products
        String[] unhealthyProducts = {
            "cola drinks", "sweetened beverages", "packaged cakes", "instant noodles", "chips", "deep-fried snacks", 
            "biscuits", "sugary snacks", "processed meats", "canned foods", "sugary cereals", "processed cheese", 
            "ice cream", "energy drinks", "alcohol", "chocolate bars", "soda", "packaged desserts", "vada pav", 
            // Add more products here...
        };

        // Moderate Products
        String[] moderateProducts = {
            "cooked rice", "bread", "pasta", "pre-cooked meals", "ready-to-eat snacks", "frozen vegetables", "packed juice",
            "frozen pizza", "frozen burgers", "cheese sandwiches", "potato chips", "processed meat", "baked snacks", 
            "biscuits", "tea", "coffee", "sauces", "mayo", "salads with dressing", 
            // Add more products here...
        };

        // Healthy Products
        String[] healthyProducts = {
            "fresh fruits", "vegetable salad", "grilled chicken", "boiled vegetables", "green tea", "almond milk", "quinoa", 
            "brown rice", "multigrain bread", "lentil soup", "chia pudding", "smoothie", "yogurt", "tofu", "salmon", 
            "fruit juices", "oatmeal", "vegan snacks", "whole wheat pasta", "stir-fried vegetables", 
            // Add more products here...
        };

        // Check unhealthy ingredients
        for (String item : unhealthyIngredients) {
            if (input.contains(item)) return 2;
        }

        // Check moderate ingredients
        for (String item : moderateIngredients) {
            if (input.contains(item)) return 5;
        }

        // Check healthy ingredients
        for (String item : healthyIngredients) {
            if (input.contains(item)) return 9;
        }

        // Check unhealthy brands
        for (String item : unhealthyBrands) {
            if (input.contains(item)) return 2;
        }

        // Check moderate brands
        for (String item : moderateBrands) {
            if (input.contains(item)) return 5;
        }

        // Check healthy brands
        for (String item : healthyBrands) {
            if (input.contains(item)) return 9;
        }

        // Check unhealthy products
        for (String item : unhealthyProducts) {
            if (input.contains(item)) return 2;
        }

        // Check moderate products
        for (String item : moderateProducts) {
            if (input.contains(item)) return 5;
        }

        // Check healthy products
        for (String item : healthyProducts) {
            if (input.contains(item)) return 9;
        }

        return 6; // Default score if no match is found
    }
   

    // 🔗 USDA FoodData Central API fetching logic
    public static String fetchUSDAData(String query) {
        String apiKey = "6vJPdvHH2hRXnDvCfKYbJbwDqHIo34fkHwufNNOO";
        String url = "https://api.nal.usda.gov/fdc/v1/foods/search?query=" + query + "&api_key=" + apiKey;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Error: Unable to fetch USDA data. Please try again.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching data.";
        }
    }

    // Parse USDA JSON data and extract relevant information
    public static String parseUSDAData(String jsonData) {
        StringBuilder data = new StringBuilder();

        try {
            JSONObject jsonResponse = new JSONObject(jsonData);
            JSONArray foods = jsonResponse.getJSONArray("foods");

            if (foods.length() > 0) {
                JSONObject foodItem = foods.getJSONObject(0); // Get the first result
                data.append("<b>Food Item:</b> " + foodItem.getString("description") + "<br>");

                // Extract nutrients (if available)
                if (foodItem.has("foodNutrients")) {
                    JSONArray nutrients = foodItem.getJSONArray("foodNutrients");
                    for (int i = 0; i < nutrients.length(); i++) {
                        JSONObject nutrient = nutrients.getJSONObject(i);
                        String nutrientName = nutrient.getString("nutrientName");
                        double value = nutrient.getDouble("value");
                        String unit = nutrient.getString("unitName");

                        data.append("<b>" + nutrientName + ":</b> " + value + " " + unit + "<br>");
                    }
                }
            } else {
                data.append("<b>No data available for this product.</b><br>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            data.append("<b>Error parsing USDA data.</b><br>");
        }

        return data.toString();
    }

    // Product rating based on nutritional data
    public static String rateProductBasedOnNutrition(String jsonData) {
        try {
            JSONObject jsonResponse = new JSONObject(jsonData);
            JSONArray foods = jsonResponse.getJSONArray("foods");

            if (foods.length() > 0) {
                JSONObject foodItem = foods.getJSONObject(0);
                JSONArray nutrients = foodItem.getJSONArray("foodNutrients");

                double calories = 0;
                double fat = 0;
                double sugar = 0;

                for (int i = 0; i < nutrients.length(); i++) {
                    JSONObject nutrient = nutrients.getJSONObject(i);
                    String nutrientName = nutrient.getString("nutrientName");

                    if (nutrientName.equalsIgnoreCase("Energy")) {
                        calories = nutrient.getDouble("value");
                    } else if (nutrientName.equalsIgnoreCase("Total lipid (fat)")) {
                        fat = nutrient.getDouble("value");
                    } else if (nutrientName.equalsIgnoreCase("Sugars, total")) {
                        sugar = nutrient.getDouble("value");
                    }
                }

                if (calories > 300 || fat > 20 || sugar > 15) {
                    return "Poor Rating (High in calories, fat, or sugar)";
                } else if (calories > 150 || fat > 10 || sugar > 5) {
                    return "Moderate Rating (Good, but be cautious with portions)";
                } else {
                    return "Good Rating (Healthy choice)";
                }
            } else {
                return "No Rating (Product data not available)";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calculating product rating";
        }
    }

    // Provide a healthier alternative based on the food input
    public static String getHealthierAlternative(String input) {
        // Simple database of unhealthy food and healthier alternatives
        if (input.toLowerCase().contains("soda")) {
            return "Try water, green tea, or coconut water!";
        } else if (input.toLowerCase().contains("chips")) {
            return "Opt for roasted nuts or air-popped popcorn!";
        } else if (input.toLowerCase().contains("pizza")) {
            return "Try a cauliflower crust pizza with lots of veggies!";
        } else if (input.toLowerCase().contains("ice cream")) {
            return "Go for frozen yogurt or a banana-based ice cream!";
        } else {
            return "No alternative available, but try to choose whole foods!";
        }
    }
}
