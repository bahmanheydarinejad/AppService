package ir.appservice_v1.view.beanComponents.elementBean;

import ir.appservice_v1.view.beanComponents.BaseBean;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
@RequestScope
public class EditorView extends BaseBean {

    private String text;

    private String text2;
}
