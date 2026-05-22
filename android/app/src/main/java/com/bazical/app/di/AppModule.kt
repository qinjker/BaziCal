package com.bazical.app.di

import com.bazical.app.data.remote.api.BaziApi
import com.bazical.app.data.remote.interceptor.SignatureInterceptor
import com.bazical.app.data.repository.BaziRepositoryImpl
import com.bazical.app.data.repository.FeedbackRepositoryImpl
import com.bazical.app.data.repository.UserRepositoryImpl
import com.bazical.app.domain.repository.BaziRepository
import com.bazical.app.domain.repository.FeedbackRepository
import com.bazical.app.domain.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://rili.jingyan99.com/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideSignatureInterceptor(): SignatureInterceptor {
        return SignatureInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(signatureInterceptor: SignatureInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(signatureInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideBaziApi(retrofit: Retrofit): BaziApi {
        return retrofit.create(BaziApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBaziRepository(impl: BaziRepositoryImpl): BaziRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository {
        return impl
    }
}