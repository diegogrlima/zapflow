package com.zapflow.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ImportResultTest {

    @Test
    void rejectsNegativeImportedCount() {
        assertThatThrownBy(() -> new ImportResult(-1, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectedRowsAreProtectedFromExternalChanges() {
        List<RejectedRow> rejected = new ArrayList<>(List.of(new RejectedRow(2, "motivo")));
        ImportResult result = new ImportResult(0, rejected);

        rejected.add(new RejectedRow(3, "outro motivo"));

        assertThat(result.rejected()).hasSize(1);
        assertThatThrownBy(() -> result.rejected().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
