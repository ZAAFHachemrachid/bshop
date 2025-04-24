# B-Shop Android App Architecture (Local Version)

## 1. System Architecture

The app follows Clean Architecture principles with three main layers:

```mermaid
graph TB
    A[Presentation Layer] --> B[Domain Layer]
    B --> C[Data Layer]
    
    subgraph Presentation Layer
        A1[Activities/Fragments] --> A2[ViewModels]
        A2 --> A3[UI State]
    end
    
    subgraph Domain Layer
        B1[Use Cases] --> B2[Repositories Interface]
        B1 --> B3[Domain Models]
    end
    
    subgraph Data Layer
        C1[Repository Impl] --> C2[Local Data Source]
        C2 --> C4[(Room Database)]
    end
```

## 2. Database Schema (Local Room Database)

```mermaid
erDiagram
    User ||--o{ Order : places
    User ||--o{ Review : writes
    User ||--o{ WishList : has
    Product ||--o{ Review : receives
    Product ||--o{ OrderItem : contains
    Order ||--|{ OrderItem : includes
    Category ||--o{ Product : contains

    User {
        int userId PK
        string email
        string name
        string phone
        datetime created_at
    }

    Product {
        int productId PK
        string name
        string description
        float price
        int category_id FK
        string image_path
        int stock
        float rating
    }

    Category {
        int categoryId PK
        string name
        string description
        string image_path
    }

    Order {
        int orderId PK
        int userId FK
        datetime order_date
        string status
        float total_amount
    }

    Review {
        int reviewId PK
        int userId FK
        int productId FK
        int rating
        string comment
        datetime created_at
    }
```

## 3. Key Features & Flows

### Local Authentication System
- Simple username/password login
- Local profile management
- Saved user preferences

### Product Management
- Category-based browsing
- Product search with filters
- Detailed product views
- Local product reviews and ratings

### Shopping Experience
- Shopping cart management
- Wishlist functionality
- Order history
- Local order tracking

### Review System
- Product ratings stored locally
- Written reviews
- View all reviews per product

## 4. Technical Stack

### Android Components
- Java as primary language
- MVVM architecture pattern
- AndroidX libraries
- Material Design components

### Local Storage
- Room Database for all data persistence
- SharedPreferences for user settings and app state
- Local file storage for product images

### Third-party Libraries
- Glide for image loading
- Navigation component
- LiveData/ViewModel

## 5. Project Structure

```
com.example.b_shop/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entities/
│   │   └── AppDatabase
│   └── repositories/
├── domain/
│   ├── models/
│   ├── repositories/
│   └── usecases/
├── ui/
│   ├── auth/
│   ├── cart/
│   ├── category/
│   ├── product/
│   ├── profile/
│   └── wishlist/
└── utils/
```

## Implementation Notes

1. All data will be stored locally using Room Database
2. Product images will be stored in app's local storage
3. Authentication will be handled with local user accounts
4. Sample data will be preloaded for categories and products
5. Orders and reviews will persist locally