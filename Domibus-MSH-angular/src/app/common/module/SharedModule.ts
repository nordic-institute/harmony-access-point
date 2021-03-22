import {NgModule} from "@angular/core";

import {ClickStopPropagationDirective} from 'app/common/directive/attribute/ClickStopPropagation';
import {ButtonClickBehaviourDirective} from '../directive/ButtonClickBehaviour';
import {AdvancedFilterBehaviourDirective} from '../directive/AdvancedFilterBehaviour';
import {InputDebounceBehaviourDirective} from '../directive/InputDebounceBehaviour';

@NgModule({
  declarations: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective,
    AdvancedFilterBehaviourDirective,
    InputDebounceBehaviourDirective,
  ],
  exports: [
    ClickStopPropagationDirective,
    ButtonClickBehaviourDirective,
    AdvancedFilterBehaviourDirective,
    InputDebounceBehaviourDirective,
  ]
})
export class SharedModule {
}
