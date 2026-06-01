package com.niki914.breeno.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.niki914.breeno.viewmodel.OSIntent
import com.niki914.breeno.viewmodel.OtherSettingsViewModel
import com.niki914.core.R.string

@Composable
fun OpenAIRulesScreen(
    onBack: () -> Unit = {}
) {
    val viewModel: OtherSettingsViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    val openAirRules = state.value.openAirRules

    var showAddRuleDialog by remember { mutableStateOf(false) }
    var ruleId by remember { mutableStateOf("") }
    var regex by remember { mutableStateOf("") }
    var headerKey by remember { mutableStateOf("") }
    var headerValue by remember { mutableStateOf("") }
    var bodyKey by remember { mutableStateOf("") }
    var bodyValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BaseTopBar(
                string.openai_rules_ui_string,
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = iconColors
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 规则列表
            openAirRules.forEach { rule ->
                RuleItem(
                    rule = rule,
                    onEdit = {
                        ruleId = rule.id
                        regex = rule.regex
                        headerKey = rule.headers.keys.firstOrNull() ?: ""
                        headerValue = rule.headers.values.firstOrNull() ?: ""
                        bodyKey = rule.body.keys.firstOrNull() ?: ""
                        bodyValue = rule.body.values.firstOrNull() ?: ""
                        showAddRuleDialog = true
                    },
                    onDelete = {
                        // 实现删除逻辑
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 添加规则按钮
            Button(
                onClick = { showAddRuleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(string.add_rule)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(string.add_rule))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 添加/编辑规则对话框
        if (showAddRuleDialog) {
            AddEditRuleDialog(
                ruleId = ruleId,
                regex = regex,
                headerKey = headerKey,
                headerValue = headerValue,
                bodyKey = bodyKey,
                bodyValue = bodyValue,
                onConfirm = {
                    val newRule = RuleModel(
                        id = ruleId,
                        regex = regex,
                        headers = mapOf(headerKey to headerValue),
                        body = mapOf(bodyKey to bodyValue)
                    )

                    // 更新规则列表
                    val updatedRules = if (ruleId.isEmpty()) {
                        openAirRules + newRule
                    } else {
                        openAirRules.map { if (it.id == ruleId) newRule else it }
                    }

                    viewModel.sendIntent(OSIntent.SaveValue(Key.OpenAIRules, updatedRules))
                    showAddRuleDialog = false
                },
                onCancel = { showAddRuleDialog = false }
            )
        }
    }
}

@Composable
fun RuleItem(
    rule: RuleModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = rule.regex,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Headers: ${rule.headers.entries.joinToString(", ") { "${it.key}=${it.value}" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Body: ${rule.body.entries.joinToString(", ") { "${it.key}=${it.value}" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onEdit
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(string.edit)
            )
        }
        IconButton(
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(string.delete)
            )
        }
    }
}

@Composable
fun AddEditRuleDialog(
    ruleId: String,
    regex: String,
    headerKey: String,
    headerValue: String,
    bodyKey: String,
    bodyValue: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = if (ruleId.isEmpty()) stringResource(string.add_rule) else stringResource(string.edit_rule)
            )
        },
        text = {
            Column {
                CommonOutlinedTextField(
                    value = regex,
                    onValueChange = { regex = it },
                    label = stringResource(string.regex_pattern),
                    description = stringResource(string.regex_pattern_description),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(string.headers),
                    style = MaterialTheme.typography.titleMedium
                )

                CommonOutlinedTextField(
                    value = headerKey,
                    onValueChange = { headerKey = it },
                    label = stringResource(string.header_key),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                CommonOutlinedTextField(
                    value = headerValue,
                    onValueChange = { headerValue = it },
                    label = stringResource(string.header_value),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(string.body),
                    style = MaterialTheme.typography.titleMedium
                )

                CommonOutlinedTextField(
                    value = bodyKey,
                    onValueChange = { bodyKey = it },
                    label = stringResource(string.body_key),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                CommonOutlinedTextField(
                    value = bodyValue,
                    onValueChange = { bodyValue = it },
                    label = stringResource(string.body_value),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(string.save))
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text(stringResource(string.cancel))
            }
        }
    )
}