package io.apicurio.registry.rest;

import io.apicurio.registry.types.RuleType;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the shared REST parameter validation helpers.
 */
class ParameterValidationUtilsTest {

    @Test
    void validatesRuleConfigurations() {
        assertDoesNotThrow(() -> ParameterValidationUtils.validateRuleConfiguration(
                RuleType.VALIDITY, "FULL"));
        assertDoesNotThrow(() -> ParameterValidationUtils.validateRuleConfiguration(
                RuleType.COMPATIBILITY, "BACKWARD_TRANSITIVE"));
        assertDoesNotThrow(() -> ParameterValidationUtils.validateRuleConfiguration(
                RuleType.INTEGRITY, "REFS_EXIST, NO_DUPLICATES"));
    }

    @Test
    void rejectsUnknownRuleConfigurations() {
        for (RuleType ruleType : RuleType.values()) {
            assertThrows(InvalidParameterValueException.class,
                    () -> ParameterValidationUtils.validateRuleConfiguration(ruleType, "UNKNOWN"));
        }
    }

    @Test
    void reportsAcceptedRuleConfigurations() {
        InvalidParameterValueException validityError = assertThrows(InvalidParameterValueException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(
                        RuleType.VALIDITY, "UNKNOWN"));
        assertTrue(validityError.getMessage().contains("[NONE, SYNTAX_ONLY, FULL]"));

        InvalidParameterValueException integrityError = assertThrows(InvalidParameterValueException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(
                        RuleType.INTEGRITY, "UNKNOWN"));
        assertTrue(integrityError.getMessage().contains("one or more comma-separated values"));
        assertTrue(integrityError.getMessage().contains("NO_CIRCULAR_REFERENCES"));
    }

    @Test
    void rejectsMalformedIntegrityConfiguration() {
        assertThrows(InvalidParameterValueException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(
                        RuleType.INTEGRITY, "REFS_EXIST,,NO_DUPLICATES"));
    }

    @Test
    void preservesStrictValidityConfigurationSemantics() {
        assertThrows(InvalidParameterValueException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(RuleType.VALIDITY, " FULL "));
    }

    @Test
    void rejectsMissingRuleConfiguration() {
        assertThrows(MissingRequiredParameterException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(null, "FULL"));
        assertThrows(MissingRequiredParameterException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(RuleType.VALIDITY, null));
        assertThrows(MissingRequiredParameterException.class,
                () -> ParameterValidationUtils.validateRuleConfiguration(RuleType.VALIDITY, "  "));
    }

    @Test
    void normalizeOffsetClampsNegativeToZero() {
        assertEquals(0, ParameterValidationUtils.normalizeOffset(BigInteger.valueOf(-1)));
    }

    @Test
    void normalizeOffsetClampsOverflowToMaxInt() {
        BigInteger overflow = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        assertEquals(Integer.MAX_VALUE, ParameterValidationUtils.normalizeOffset(overflow));
    }

    @Test
    void normalizeOffsetPassesThroughValidValue() {
        assertEquals(42, ParameterValidationUtils.normalizeOffset(BigInteger.valueOf(42)));
    }

    @Test
    void normalizeLimitNormalizesNegativeToOne() {
        assertEquals(1, ParameterValidationUtils.normalizeLimit(BigInteger.valueOf(-1)));
    }

    @Test
    void normalizeLimitPreservesZero() {
        assertEquals(0, ParameterValidationUtils.normalizeLimit(BigInteger.ZERO));
    }

    @Test
    void normalizeLimitCapsAboveOneThousand() {
        assertEquals(1000, ParameterValidationUtils.normalizeLimit(BigInteger.valueOf(5000)));
    }

    @Test
    void normalizeLimitUnboundedNormalizesNegativeToOne() {
        assertEquals(1, ParameterValidationUtils.normalizeLimitUnbounded(BigInteger.valueOf(-1)));
    }

    @Test
    void normalizeLimitUnboundedPreservesZero() {
        assertEquals(0, ParameterValidationUtils.normalizeLimitUnbounded(BigInteger.ZERO));
    }

    @Test
    void normalizeLimitUnboundedDoesNotCapAboveOneThousand() {
        assertEquals(5000, ParameterValidationUtils.normalizeLimitUnbounded(BigInteger.valueOf(5000)));
    }

    @Test
    void normalizeLimitUnboundedClampsOverflowToMaxInt() {
        BigInteger overflow = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE);
        assertEquals(Integer.MAX_VALUE, ParameterValidationUtils.normalizeLimitUnbounded(overflow));
    }

}
