package kr.kro.dearmoment.product.adapter.input.web.search

import kr.kro.dearmoment.common.MockBaseApiTest
import kr.kro.dearmoment.product.adapter.input.web.ProductRestAdapter
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

@WebMvcTest(ProductRestAdapter::class)
class SearchProductRestAdapterTest : MockBaseApiTest()
