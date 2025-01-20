import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.snippet.Attributes
import org.springframework.restdocs.snippet.Attributes.Attribute
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

object RestDocsUtils {
    /**
     * 'default' 키에 해당하는 Attribute 객체를 생성하여 반환합니다.
     *
     * @param value 'default' 키에 설정할 값입니다.
     * @return 'default' 키와 주어진 값을 포함하는 Attribute 객체
     */
    fun defaultValue(value: String): Attribute {
        return Attributes.key("default").value(value)
    }

    /**
     * 'format' 키에 해당하는 Attribute 객체를 생성하여 반환합니다.
     *
     * @param value 'format' 키에 설정할 값입니다.
     * @return 'format' 키와 주어진 값을 포함하는 Attribute 객체
     */
    fun customFormat(value: String): Attribute {
        return Attributes.key("format").value(value)
    }

    /**
     * 'example' 키에 해당하는 Attribute 객체를 생성하여 반환합니다.
     *
     * @param value 'example' 키에 설정할 값입니다.
     * @return 'example' 키와 주어진 값을 포함하는 Attribute 객체
     */
    fun customExample(value: String): Attribute {
        return Attributes.key("example").value(value)
    }
}

/**
 * `ResultActions` 객체에 RestDocs 문서화 스니펫을 추가하는 확장 함수입니다.
 * 이 함수는 요청과 응답의 예시를 출력하고 문서화할 때 사용됩니다.
 *
 * @param identifier 문서화할 문서의 고유 식별자
 * @param snippets RestDocs 스니펫 배열
 * @return 문서화된 `ResultActions` 객체
 */
fun ResultActions.andDocument(
    identifier: String,
    vararg snippets: Snippet,
): ResultActions {
    return andDo(
        document(
            // 문서화 식별자
            identifier,
            // 요청을 보기 좋게 포맷
            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
            // 응답을 보기 좋게 포맷
            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
            // 스니펫을 추가하여 문서화
            *snippets,
        ),
    )
}

/**
 * RestDocs 문서화를 위한 `MockMvc` 객체를 설정하는 함수입니다.
 * Spring WebApplicationContext와 RestDocumentationContextProvider를 사용하여 MockMvc를 초기화하고,
 * 요청 및 응답을 예쁘게 출력하도록 설정합니다.
 *
 * @param context `WebApplicationContext` 객체
 * @param provider `RestDocumentationContextProvider` 객체
 * @return 설정된 `MockMvc` 객체
 */
fun restDocMockMvcBuild(
    context: WebApplicationContext,
    provider: RestDocumentationContextProvider,
): MockMvc {
    return MockMvcBuilders
        // 웹 애플리케이션 컨텍스트 설정
        .webAppContextSetup(context)
        .apply<DefaultMockMvcBuilder>(
            // RestDocs 설정
            MockMvcRestDocumentation.documentationConfiguration(provider)
                .operationPreprocessors()
                // 요청 예쁘게 포맷
                .withRequestDefaults(Preprocessors.prettyPrint())
                // 응답 예쁘게 포맷
                .withResponseDefaults(Preprocessors.prettyPrint()),
        )
        // .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity(MockSecurityFilter())) security 추가 이후
        .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true)) // UTF-8 문자 인코딩 필터 추가
        .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print()) // 결과 출력
        .build() // MockMvc 객체 빌드
}
