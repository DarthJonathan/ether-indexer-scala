package id.trakkie.payment.impl.utils

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import id.trakkie.payment.impl.config.ConfigHolder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StripeUtilities extends ConfigHolder{

  def generatePaymentLink(paymentAmount: Long): Future[Session] = {
    Stripe.apiKey = config.getString("STRIPE_API_KEY")
    val paymentParams: SessionCreateParams = SessionCreateParams.builder()
      .setMode(SessionCreateParams.Mode.PAYMENT)
      .setSuccessUrl("https://example.com/success")
      .setCancelUrl("https://example.com/cancel")
      .addLineItem(
        SessionCreateParams.LineItem.builder()
          .setQuantity(1L)
          .setPriceData(
            SessionCreateParams.LineItem.PriceData.builder()
              .setCurrency("usd")
              .setUnitAmount(paymentAmount)
              .setProductData(
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                  .setName("T-shirt")
                  .build())
              .build())
          .build())
      .build()

    Future.apply(Session.create(paymentParams))
  }

}
