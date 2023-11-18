package com.mazizs.clothes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.mazizs.clothes.data.Datasource
import com.mazizs.clothes.model.Clothes
import com.mazizs.clothes.ui.theme.ClothesTheme

//Tanda untuk masuk
private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate Called") //Untuk mencetak pesan log "onCreate Called" ke logcat
        setContent {
            ClothesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ClothesApp(clothes = Datasource.clothesList)
                }
            }
        }
    }

    //Di bawah ini merupakan untuk memantau siklus aktivitas MainActivity dan mencetak pesan log sesuai dengan setiap peristiwa siklus
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Called") //Untuk mencetak pesan log "onStart Called" ketika memulai aktivitas
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume Called") //Untuk encetak pesan log "onResume Called" ketika aktivitas kembali dari status dijeda atau resume
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart Called") //Umtuk mencetak pesan log "onRestart Called" ketika aktivitas dimulai ulang
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause Called") //Untuk mencetak pesan log "onPause Called" ketika aktivitasdijeda atau
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop Called") //Untuk mencetak pesan log "onStop Called" ketika aktivitas dihentikan atau berpindah ke latar belakang
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy Called") //Untuk mencetak pesan log "onDestroy Called" ketika aktivitas ditutup

    }
}

//Di bawah ini untuk menentukan jenis pakaian yang akan ditampilkan berdasarkan jumlah pakaian yang telah terjual dan daftar pakaian yang tersedia
fun determineClothesToShow(
    clothes: List<Clothes>,
    clothesSold: Int
): Clothes {
    var clothesToShow = clothes.first() //Untuk meginisialisasi pakaian yang akan ditampilkan dengan item pertama dalam daftar
    for (clothes in clothes) {
        //Memeriksa apakah jumlah yang terjual lebih besar atau sama dengan (mulai Jumlah Produksi)startProductionAmount dari pakaian saat ini
        //Jika true, maka akan memperbarui pakaian yang akan ditampilkan dengan pakaian saat ini
        if (clothesSold >= clothes.startProductionAmount) {
            clothesToShow = clothes
        } else {
        //Daftar pakaian diurutkan berdasarkan (mulai Jumlah Produksi) startProductionAmount
        //Ketika pakaian terjual lebih bnyak, maka akan memproduksi pakaian yang lebih mahal sesuai dengan startProductionAmount
            break
        }
    }

    return clothesToShow
}

//Membagikan informasi penjualan pakaian melalui intent
private fun shareSoldClothesInformation(intentContext: Context, clothesSold: Int, revenue: Int) {
    val sendIntent = Intent().apply {//Untuk membuat intent dengan menggunakan aksi ACTION_SEND untuk berbagi atau share
        action = Intent. ACTION_SEND
        putExtra( //Untuk membuat teks yang akan dibagikan
            Intent.EXTRA_TEXT,
            intentContext.getString(R.string.share_text, clothesSold, revenue)
        )
        type = "text/plain"
    }
    //Membuat dapat memilih saat akan dibagikan oleh pengguna
    val shareIntent = Intent.createChooser(sendIntent, null)

    try {
        ContextCompat.startActivity(intentContext, shareIntent, null)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            intentContext,
            intentContext.getString(R.string.sharing_not_available),
            Toast.LENGTH_LONG
        ).show()
    }
}

//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi ClothesApp yaitu untuk menampilkam pakaian dan harganya
@Composable
private fun ClothesApp(
    clothes: List<Clothes>
) {
    //Total harga dari penjualan pakaian
    var revenue by rememberSaveable { mutableStateOf(0) }
    //Jumlah total pakaian yang terjual
    var clothesSold by rememberSaveable { mutableStateOf(0) }
    //Menampilkan pakaian saat ini
    val currentClothesIndex by rememberSaveable { mutableStateOf(0) }
    //Harga pakaian saat ini
    var currentClothesPrice by rememberSaveable {
        mutableStateOf(clothes[currentClothesIndex].price)
    }
    var currentClothesImageId by rememberSaveable { //Gambar pakaian saat ini
        mutableStateOf(clothes[currentClothesIndex].imageId)
    }

    Scaffold( //Untuk membuat tata letak dengan menggunakan komponen Scaffold dari Jetpack Compose

        topBar = {
            val intentContext = LocalContext.current
            ClothesClickerAppBar( //Untukmenampilkan AppBar dengan tombol berbagi yang dipanggil dari fungsi shareSoldClothesInformation

                onShareButtonClicked = {
                    shareSoldClothesInformation(
                        intentContext = intentContext,
                        clothesSold = clothesSold,
                        revenue = revenue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    ) { contentPadding ->
        //untuk menampilkan layar utama ClothesClicker dengan informasi pendapatan, jumlah pakaian terjual, gambar pakaian, dan fungsi yang dipanggil saat pakaian diklik
        ClothesClickerScreen(
            revenue = revenue,
            clothesSold = clothesSold,
            clothesImageId = currentClothesImageId,
            onClothesClicked = {

                //Memperbarui harga
                revenue += currentClothesPrice
                clothesSold++

                //Menampilkan pakaian selanjutnya dan harga
                val clothesToShow = determineClothesToShow(clothes, clothesSold)
                currentClothesImageId = clothesToShow.imageId
                currentClothesPrice = clothesToShow.price
            },
            modifier = Modifier.padding(contentPadding)
        )
    }
}

//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi ClothesClickerAppBar yaitu untuk menampilkan judul aplikasi dan tombol berbagi
@Composable
private fun ClothesClickerAppBar(
    onShareButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row( //Untuk mengatur tata letak elemen-elemen dalam satu baris
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text( //Untuk menampilkan teks judul aplikasi dengan style dan warna yang sesuai dari [MaterialTheme]
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_medium)),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton( //Untuk menambahkan tombol ikon untuk berbagi
            onClick = onShareButtonClicked,
            modifier = Modifier.padding(end = dimensionResource(R.dimen.padding_medium)),
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(R.string.share),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi ClothesClickerScreen yaitu untuk menampilkan layar Clothes Clicker
@Composable
fun ClothesClickerScreen(
    revenue: Int,
    clothesSold: Int,
    @DrawableRes clothesImageId: Int,
    onClothesClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {//Merupakan kontainer yang menempatkan elemen-elemen ditumpukan
        Image( //Untuk menampilkan gambar background kayu
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Column {//Untuk mengatur elemen-elemen secara vertikal, menempatkan gambar dan informasi total harga
            Box( //Untuk menampilkan gambar pakaian yang bisa diklik
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Image( //Untuk menampilkan gambar pakaian dengan ukuran tertentu, posisi ditengah, dan efek ketika diklik
                    painter = painterResource(clothesImageId),
                    contentDescription = null,
                    modifier = Modifier
                        .width(dimensionResource(R.dimen.image_size))
                        .height(dimensionResource(R.dimen.image_size))
                        .align(Alignment.Center)
                        .clickable { onClothesClicked() },
                    contentScale = ContentScale.Crop,
                )
            }
            TransactionInfo( //Unrtuk menampilkan informasi transaksi seperti jumlah pakaian yang terjual terjual dan total harga
                revenue = revenue,
                clothesSold = clothesSold,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            )
        }
    }
}

//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi TransactionInfo yaitu untuk menampilkan informasi transaksi atau penjualan dalam bentuk UI
@Composable
private fun TransactionInfo(
    revenue: Int,
    clothesSold: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {//Untuk membuat kolom secara vertikal dan untuk menampung informasi penjualan
        ClothesSoldInfo( //Untuk menampilkan informasi jumlah pakaian yang terjual
            clothesSold = clothesSold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
        RevenueInfo( //Untuk menampilkan informasi total harga
            revenue = revenue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_medium))
        )
    }
}

//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi RevenueInfo yaitu untuk membuat tampilan UI yang menampilkan informasi total harga
@Composable
private fun RevenueInfo(revenue: Int, modifier: Modifier = Modifier) {
    Row( //Untuk mengatur elemen-elemen secara horizontal
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text( //Untuk menampilkan teks Total Harga dengan style teks headlineMedium dan warna sesuai tema
            text = stringResource(R.string.total_harga),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text( //Untuk menampilkan teks Rp dan style teks headlineMedium, serta warna sesuai tema
            text = "Rp${revenue}",
            textAlign = TextAlign.Right,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
//Fungsi komponen Composable dalam Jetpack Compose di bawah ini merupakan fungsi ClothesSoldInfo yaitu untuk membuat komponen Compose yang menampilkan informasi tentang jumlah pakaian yang telah terjual
@Composable
private fun ClothesSoldInfo(clothesSold: Int, modifier: Modifier = Modifier) {
    Row( //untuk mengatur letak elemen-elemen secara horizontal
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text( //Untuk menampilkan teks Pakaian Terjual
            text = stringResource(R.string.pakaian_terjual),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(  //Untuk menampilkan jumlah pakaian yang terjual
            text = clothesSold.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

//Fungsi di bawah ini adalah komponen Composable yang digunakan untuk menampilkan preview atau pratinjau
@Preview
@Composable
fun MyClothesClickerAppPreview() {
    ClothesTheme {
        ClothesApp(listOf(Clothes(R.drawable.tshirt, 17000, 0)))
    }
}