package me.mrafonso.runway.resolver

import com.ezylang.evalex.Expression
import me.mrafonso.runway.config.placeholder.ConditionalPlaceholder
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.MatchPlaceholder
import me.mrafonso.runway.config.placeholder.SwitchPlaceholder
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag

class PlaceholderEvaluator(
    private val miniMessage: MiniMessage,
    private val placeholderProcessor: (String, Pointered?) -> Component?
) {

    /**
     * Evaluates a [ConditionalPlaceholder] and returns the appropriate [Tag] based on the condition.
     *
     * @param group The [Group] the placeholder belongs to.
     * @param placeholder The [ConditionalPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the condition.
     */
    fun evaluateConditional(group: Group, placeholder: ConditionalPlaceholder, target: Pointered?, deserialize: (String, Pointered?) -> Component): Tag {
        println("- ${placeholder.condition}")
        println("+ ${group.condition}")
        val conditionResult = evaluateExpression(placeholder.condition, target)
        val groupResult = evaluateGroupCondition(group, target)

        val output = when {
            conditionResult && groupResult -> placeholder.ifTrue
            !conditionResult && placeholder.ifElse != null -> placeholder.ifElse
            else -> ""
        }

        return Tag.inserting(deserialize(output, target))
    }

    /**
     * Evaluates a [MatchPlaceholder] and returns the appropriate [Tag] based on the matching case.
     *
     * @param placeholder The [MatchPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the match cases.
     */
    fun evaluateMatch(placeholder: MatchPlaceholder, target: Pointered?, deserialize: (String, Pointered?) -> Component): Tag {
        val input = placeholderProcessor(placeholder.input, target)

        val match = placeholder.case.firstOrNull {
            val processedKey = placeholderProcessor(it.comparison, target)
            input == processedKey
        }

        val output = match?.output ?: placeholder.default
        return Tag.inserting(deserialize(output, target))
    }

    /**
     * Evaluates a [SwitchPlaceholder] and returns the appropriate [Tag] based on the evaluated case.
     *
     * @param placeholder The [SwitchPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the switch cases.
     */
    fun evaluateSwitch(placeholder: SwitchPlaceholder, target: Pointered?, deserialize: (String, Pointered?) -> Component): Tag {
        val match = placeholder.case.firstOrNull {
            evaluateExpression(placeholder.input + it.comparison, target)
        }

        val output = match?.output ?: placeholder.default
        return Tag.inserting(deserialize(output, target))
    }

    /**
     * Evaluates a boolean expression using the expression parser.
     *
     * @param expression The expression to evaluate.
     * @return [Boolean] The result of the evaluated expression.
     */
    fun evaluateExpression(expression: String, target: Pointered?): Boolean {
        placeholderProcessor(expression, target)?.let {
            val condition = miniMessage.serialize(it)
                .replace("\\<", "<").replace("\\>", ">")
            return Expression(condition).evaluate().booleanValue
        }
        return true
    }

    /**
     * Evaluates the condition of a group if it exists.
     *
     * @param group The [Group] whose condition is to be evaluated.
     * @return [Boolean] The result of the group's condition evaluation, or true if no condition exists.
     */
    fun evaluateGroupCondition(group: Group, target: Pointered?): Boolean {
        if (group.condition == null) return true
        return evaluateExpression(group.condition, target)
    }
}
