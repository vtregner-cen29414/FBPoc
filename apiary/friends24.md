FORMAT: 1A
HOST: http://www.google.com

# Friends24
Friends24 API is a Servis24 API for Facebook

## Login [/login]
### Login into Friends24 application [POST]
+ Request (application/x-www-form-urlencoded)

        username=ferda&password=heslo

+ Response 200


## Accounts Collection [/accounts]
### List all user bank accounts [GET]
+ Response 200 (application/json)

        {"accounts":
            [{
              "prefix": 13, "number": 123123, "type": 1, "currency": "CZK", "balance": 58764
            }, {
              "number": 123123, "type": 2, "currency": "CZK", "balance": 764
            }]
        }


## Payments overview [/payments/{prefix-}{accountnumber}]
### List of user payments with its current status [GET]
+ Parameters
    + prefix (optional, number) ... prefix of source account number
    + accountnumber (number) ... source account number of payments, formatted to prefix-number
+ Response 200 (application/json)

        {"payments":
            [{
              "id": "1G3Xc3", "recipientid": 123123, "recipientname": "Pepa Kotva", "amount": 125, "currency": "CZK", "status": 0
            }, {
              "id": "583Xy3", "recipientid": 358798, "recipientname": "Iveta Svobodová", "amount": 260, "currency": "CZK", "status": 1
            }, {
              "id": "th587s", "recipientid": 5544884, "recipientname": "Honza Sechovec", "amount": 78, "currency": "CZK", "status": 2
            }, {
              "id": "TR548v", "recipientid": 5674468, "recipientname": "Hanka Kubrichtová", "amount": 156, "currency": "CZK", "status": 2
            }]
        }



