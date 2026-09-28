package com.murali.worldfind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.murali.worldfind.data.local.PreferenceManager
import com.murali.worldfind.data.models.*
import com.murali.worldfind.data.remote.CategoryDto
import com.murali.worldfind.data.remote.CheckoutItemReq
import com.murali.worldfind.data.repository.*
import com.murali.worldfind.ui.state.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val cartRepository: CartRepository = CartRepository(),
    private val wishlistRepository: WishlistRepository = WishlistRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
    private val addressRepository: AddressRepository = AddressRepository(),
    private val preferenceManager: PreferenceManager? = null
) : ViewModel() {

    // Live Product & Category UI States
    private val _homeProductsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val homeProductsState: StateFlow<UiState<List<Product>>> = _homeProductsState.asStateFlow()

    private val _categoriesState = MutableStateFlow<UiState<List<CategoryDto>>>(UiState.Loading)
    val categoriesState: StateFlow<UiState<List<CategoryDto>>> = _categoriesState.asStateFlow()

    private val _searchResultsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val searchResultsState: StateFlow<UiState<List<Product>>> = _searchResultsState.asStateFlow()

    private val _productDetailState = MutableStateFlow<UiState<Product?>>(UiState.Loading)
    val productDetailState: StateFlow<UiState<Product?>> = _productDetailState.asStateFlow()

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistItems: StateFlow<List<Product>> = wishlistRepository.wishlistItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = orderRepository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val addresses: StateFlow<List<Address>> = addressRepository.addresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _directCheckoutItem = MutableStateFlow<CartItem?>(null)
    val directCheckoutItem = _directCheckoutItem.asStateFlow()

    private val _selectedCheckoutAddress = MutableStateFlow<Address?>(null)
    val selectedCheckoutAddress = _selectedCheckoutAddress.asStateFlow()

    // Filtering State
    private val _exploreCategoryFilter = MutableStateFlow<String?>(null)
    val exploreCategoryFilter = _exploreCategoryFilter.asStateFlow()

    private val _selectedCountries = MutableStateFlow<Set<String>>(emptySet())
    val selectedCountries = _selectedCountries.asStateFlow()

    private val _isINRAdvantageFilter = MutableStateFlow<Boolean?>(null)
    val isINRAdvantageFilter = _isINRAdvantageFilter.asStateFlow()

    init {
        loadHomeProducts()
        loadCategories()
        searchExploreProducts()
        loadBackendData()
    }

    private fun loadBackendData() {
        viewModelScope.launch {
            cartRepository.fetchCart()
            wishlistRepository.fetchWishlist()
            addressRepository.fetchAddresses()
            orderRepository.fetchOrders()
        }
    }

    fun loadHomeProducts() {
        viewModelScope.launch {
            _homeProductsState.value = UiState.Loading
            val result = productRepository.getProducts(pageSize = 30)
            if (result.isSuccess) {
                val response = result.getOrNull()
                val products = response?.products?.map { with(productRepository) { it.toDomainProduct() } } ?: emptyList()
                _homeProductsState.value = UiState.Success(products)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unable to load products. Please check your connection."
                _homeProductsState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            val result = productRepository.getCategories()
            if (result.isSuccess) {
                val cats = result.getOrNull() ?: emptyList()
                _categoriesState.value = UiState.Success(cats)
            } else {
                _categoriesState.value = UiState.Error(result.exceptionOrNull()?.message ?: "Failed to load categories")
            }
        }
    }

    fun searchExploreProducts(
        keywords: String? = null,
        categoryIds: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sort: String? = null
    ) {
        viewModelScope.launch {
            _searchResultsState.value = UiState.Loading
            val result = productRepository.getProducts(
                keywords = keywords,
                categoryIds = categoryIds,
                minSalePrice = minPrice,
                maxSalePrice = maxPrice,
                sort = sort,
                pageSize = 50
            )
            if (result.isSuccess) {
                val response = result.getOrNull()
                val products = response?.products?.map { with(productRepository) { it.toDomainProduct() } } ?: emptyList()
                _searchResultsState.value = UiState.Success(products)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unable to load products. Please try again."
                _searchResultsState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            _productDetailState.value = UiState.Loading
            val result = productRepository.getProductById(productId)
            if (result.isSuccess) {
                val product = result.getOrNull()
                if (product != null) {
                    _productDetailState.value = UiState.Success(product)
                } else {
                    _productDetailState.value = UiState.Error("Product unavailable")
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Unable to load product detail"
                _productDetailState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun setDirectCheckout(product: Product?) {
        _directCheckoutItem.value = product?.let { CartItem(it, 1) }
    }

    fun selectCheckoutAddress(address: Address?) {
        _selectedCheckoutAddress.value = address
    }

    fun setExploreCategoryFilter(category: String?) {
        _exploreCategoryFilter.value = category
    }

    fun toggleCountrySelection(countryId: String) {
        val current = _selectedCountries.value.toMutableSet()
        if (current.contains(countryId)) {
            current.remove(countryId)
        } else {
            current.add(countryId)
        }
        _selectedCountries.value = current
    }

    fun setINRAdvantageFilter(advantage: Boolean?) {
        _isINRAdvantageFilter.value = advantage
    }

    fun clearAllFilters() {
        _exploreCategoryFilter.value = null
        _selectedCountries.value = emptySet()
        _isINRAdvantageFilter.value = null
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            cartRepository.addToCart(product.id, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, quantity)
        }
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            wishlistRepository.toggleWishlist(product.id)
        }
    }

    fun isInWishlist(productId: String): Boolean {
        return wishlistRepository.isInWishlist(productId)
    }

    fun addAddress(address: Address) {
        viewModelScope.launch {
            val result = addressRepository.addAddress(address)
            if (result.isSuccess) {
                val created = result.getOrNull()
                if (_selectedCheckoutAddress.value == null || created?.isDefault == true) {
                    _selectedCheckoutAddress.value = created
                }
            }
        }
    }

    fun updateAddress(address: Address) {
        viewModelScope.launch {
            val result = addressRepository.updateAddress(address)
            if (result.isSuccess) {
                val updated = result.getOrNull()
                if (_selectedCheckoutAddress.value?.id == address.id) {
                    _selectedCheckoutAddress.value = updated
                }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(addressId)
            if (_selectedCheckoutAddress.value?.id == addressId) {
                _selectedCheckoutAddress.value = null
            }
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(addressId)
            addresses.value.find { it.id == addressId }?.let {
                _selectedCheckoutAddress.value = it
            }
        }
    }

    fun placeOrder(address: Address, paymentMethod: String): String? {
        val checkoutItems = if (_directCheckoutItem.value != null) {
            listOf(_directCheckoutItem.value!!)
        } else {
            cartItems.value
        }

        if (checkoutItems.isEmpty()) return null

        val mappedPaymentMethod = when (paymentMethod.uppercase()) {
            "COD", "CASH ON DELIVERY" -> "COD"
            "RAZORPAY" -> "RAZORPAY"
            else -> "ONLINE_PAYMENT"
        }

        val itemsReq = checkoutItems.map { CheckoutItemReq(it.product.id, it.quantity) }
        var generatedOrderId: String? = null

        viewModelScope.launch {
            val result = orderRepository.createOrder(
                addressId = address.id,
                items = if (_directCheckoutItem.value != null) itemsReq else null,
                paymentMethod = mappedPaymentMethod
            )
            if (result.isSuccess) {
                val createdOrder = result.getOrNull()
                generatedOrderId = createdOrder?.id
                if (_directCheckoutItem.value != null) {
                    _directCheckoutItem.value = null
                } else {
                    cartRepository.clearCart()
                }
                _selectedCheckoutAddress.value = null
            }
        }

        return generatedOrderId ?: ("ORD" + System.currentTimeMillis().toString().takeLast(6))
    }
}
