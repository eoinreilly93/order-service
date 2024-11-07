Feature: Example feature file

  Background:
    Given initial setup is complete
    And the service is up and running
    And now is "2024-11-06T08:13:12.345"

  @Positive
  Scenario: Successfully create an order

    When a POST request is sent to "/orders" with data
    """
      {
        "purchaseProductDTOS": [
          {
            "productId": 1,
            "quantity": 2,
            "price": 29.99
          },
          {
            "productId": 2,
            "quantity": 1,
            "price": 59.49
          }
        ],
        "city": "London"
      }
    """

    Then a response is generated with a 201 status and a body similar to
    """
      {
        "message": null,
        "error": null,
        "result": {
          "orderId": "a3e16dc7-0c39-4e30-9d53-529bacf2f3e1",
          "status": "CREATED"
        },
        "timestamp":"2024-11-06T18:14:59.346148"
       }
    """

    And contains headers
      | Location | http://localhost/orders/612d01fd-9c2b-4da3-b309-35743ed6a050" |

    And an order is saved to the database
    And the order is sent to the kafka topic "orders"