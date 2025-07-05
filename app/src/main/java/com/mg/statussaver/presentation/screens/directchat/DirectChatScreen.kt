package com.mg.statussaver.presentation.screens.directchat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Data class for country information
data class Country(
    val name: String,
    val code: String,
    val dialingCode: String,
    val flag: String // Unicode flag emoji
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    navController: NavController? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Optimized country data - prioritizing Asian countries and major regions
    val countries = remember {
        listOf(
            // Top Priority - Major Asian Countries (Your main audience)
            Country("India", "IN", "+91", "🇮🇳"),
            Country("Pakistan", "PK", "+92", "🇵🇰"),
            Country("Bangladesh", "BD", "+880", "🇧🇩"),
            Country("Nepal", "NP", "+977", "🇳🇵"),
            Country("Bhutan", "BT", "+975", "🇧🇹"),
            Country("Sri Lanka", "LK", "+94", "🇱🇰"),
            Country("Afghanistan", "AF", "+93", "🇦🇫"),
            Country("Myanmar", "MM", "+95", "🇲🇲"),
            
            // Other Popular Asian Countries
            Country("China", "CN", "+86", "🇨🇳"),
            Country("Japan", "JP", "+81", "🇯🇵"),
            Country("South Korea", "KR", "+82", "🇰🇷"),
            Country("Thailand", "TH", "+66", "🇹🇭"),
            Country("Malaysia", "MY", "+60", "🇲🇾"),
            Country("Singapore", "SG", "+65", "🇸🇬"),
            Country("Indonesia", "ID", "+62", "🇮🇩"),
            Country("Philippines", "PH", "+63", "🇵🇭"),
            Country("Vietnam", "VN", "+84", "🇻🇳"),
            Country("Cambodia", "KH", "+855", "🇰🇭"),
            
            // Major Western Countries
            Country("United States", "US", "+1", "🇺🇸"),
            Country("Canada", "CA", "+1", "🇨🇦"),
            Country("United Kingdom", "GB", "+44", "🇬🇧"),
            Country("Germany", "DE", "+49", "🇩🇪"),
            Country("France", "FR", "+33", "🇫🇷"),
            Country("Australia", "AU", "+61", "🇦🇺"),
            Country("Italy", "IT", "+39", "🇮🇹"),
            Country("Spain", "ES", "+34", "🇪🇸"),
            Country("Netherlands", "NL", "+31", "🇳🇱"),
            
            // Middle East & Gulf Countries
            Country("UAE", "AE", "+971", "🇦🇪"),
            Country("Saudi Arabia", "SA", "+966", "🇸🇦"),
            Country("Qatar", "QA", "+974", "🇶🇦"),
            Country("Kuwait", "KW", "+965", "🇰🇼"),
            Country("Bahrain", "BH", "+973", "🇧🇭"),
            Country("Oman", "OM", "+968", "🇴🇲"),
            Country("Turkey", "TR", "+90", "🇹🇷"),
            Country("Iran", "IR", "+98", "🇮🇷"),
            
            // Other Important Countries
            Country("Russia", "RU", "+7", "🇷🇺"),
            Country("Brazil", "BR", "+55", "🇧🇷"),
            Country("Mexico", "MX", "+52", "🇲🇽"),
            Country("South Africa", "ZA", "+27", "🇿🇦"),
            Country("Egypt", "EG", "+20", "🇪🇬"),
            Country("Nigeria", "NG", "+234", "🇳🇬"),
            Country("Kenya", "KE", "+254", "🇰🇪"),
            Country("Ethiopia", "ET", "+251", "🇪🇹")
        )
    }
    
    // State variables
    var selectedCountry by remember { mutableStateOf(countries[0]) } // Default to India
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    // Filter countries based on search query
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            countries
        } else {
            countries.filter { country ->
                country.name.contains(searchQuery, ignoreCase = true) ||
                country.dialingCode.contains(searchQuery, ignoreCase = true) ||
                country.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Validation logic
    val isPhoneNumberValid = phoneNumber.isNotBlank() && phoneNumber.length >= 7
    val isMessageValid = message.isNotBlank()
    val isSendButtonEnabled = isPhoneNumberValid && isMessageValid
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Direct Chat",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { navController?.navigateUp() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions text
            Text(
                text = "Send a WhatsApp message to any number without saving the contact",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Country Selector
            Text(
                text = "Select Country",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDropdownExpanded = !isDropdownExpanded },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = selectedCountry.flag,
                                fontSize = 20.sp
                            )
                            Text(
                                text = selectedCountry.name,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Text(
                                text = selectedCountry.dialingCode,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = if (isDropdownExpanded) "Collapse" else "Expand",
                            tint = Color.Gray
                        )
                    }
                }
                
                // Dropdown Menu with Search
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { 
                        isDropdownExpanded = false
                        searchQuery = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search TextField inside dropdown
                    if (isDropdownExpanded) {
                        DropdownMenuItem(
                            text = {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "Search country...",
                                            color = Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF009688),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            },
                            onClick = { /* Do nothing - let user type */ }
                        )
                    }
                    
                    // Show filtered countries
                    filteredCountries.forEach { country ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = country.flag,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = country.name,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = country.dialingCode,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            },
                            onClick = {
                                selectedCountry = country
                                isDropdownExpanded = false
                                searchQuery = ""
                            }
                        )
                    }
                    
                    // Show "No results found" if search returns empty
                    if (filteredCountries.isEmpty() && searchQuery.isNotBlank()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "No countries found",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            onClick = { /* Do nothing */ }
                        )
                    }
                }
            }
            
            // Phone Number Input
            Text(
                text = "Phone Number",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                placeholder = {
                    Text(
                        text = "Enter Your Number",
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Text(
                        text = selectedCountry.dialingCode,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF009688),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            // Message Input
            Text(
                text = "Message",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                placeholder = {
                    Text(
                        text = "Write Your Message",
                        color = Color.Gray
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                minLines = 4,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF009688),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Send Button
            Button(
                onClick = {
                    if (isSendButtonEnabled) {
                        val fullNumber = "${selectedCountry.dialingCode}$phoneNumber"
                        
                        // Open WhatsApp with the message
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://wa.me/$fullNumber?text=${Uri.encode(message)}")
                        context.startActivity(intent)
                    }
                },
                enabled = isSendButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF009688),
                    disabledContainerColor = Color(0xFFB0BEC5)
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Send Message",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DirectChatScreenPreview() {
    DirectChatScreen()
}
